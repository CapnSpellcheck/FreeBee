//
//  GameViewModel.swift
//  Free Bee
//
//  Created by G on 9/16/24.
//

import Foundation
import SwiftUI
import Combine
import CoreData

fileprivate let kMaxLetters = 19
fileprivate let kMinLetters = 4

final class GameViewModel: ObservableObject {
   let game: Game
   let objectContext: NSManagedObjectContext
   let entryNotAcceptedEvent = PassthroughSubject<Void, Never>()
   var onCurrentWordChanged: ((String?) -> Void)?

   private let userDefaults = UserDefaults.standard
   private var observationToken: NSKeyValueObservation?
      
   init(game: Game, objectContext: NSManagedObjectContext) {
      self.game = game
      if game.progress == nil {
         game.progress = GameProgress(context: objectContext)
      }
      self.objectContext = objectContext
      
      observationToken = game.progress?.observe(\.currentWord, options: [.new]) { _, change in
         self.onCurrentWordChanged?(change.newValue!)
      }
   }

   var progress: GameProgress {
      game.progress!
   }
   
   var enteredWordSummary: String {
      progress.enteredWords!.count > 0 ?
         (progress.enteredWords!.array as! Array<EnteredWord>).map {
            $0.value!.capitalized
         }.joined(separator: "\u{2003}")
      : "No words yet"
   }
   
   var enteredWordSummaryColor: Color {
      progress.enteredWords!.count > 0 ? .primary : .secondary
   }
   
   var enterEnabled: Bool {
      progress.currentWord!.count >= kMinLetters &&
         progress.currentWord!.contains(game.centerLetterCharacter)
   }
   
   func append(letter: Character) {
      if progress.currentWord?.count ?? 0 < kMaxLetters {
         objectWillChange.send()
         progress.currentWord?.append(String(letter))
      }
   }
   
   func backspace() {
      if progress.currentWord?.isEmpty != true {
         objectWillChange.send()
         progress.currentWord?.removeLast()
      }
   }
   
   func enter() {
      var errored = false
      let enteredWord = progress.currentWord!
      if game.allowedWords!.contains(enteredWord), !progress.hasEntered(word: enteredWord) {
         progress.insertIntoEnteredWords(EnteredWord(context: objectContext, string: enteredWord), at: 0)
         progress.score += scoreWord(enteredWord)
         do {
            try objectContext.save()
            if game.isPangram(word: enteredWord) {
               NSLog("pangram: %@", enteredWord)
               let pangramCountKey = UserDefaults.pangramsCount
               userDefaults[pangramCountKey] = (userDefaults[pangramCountKey] ?? 0) + 1
               NSLog("total pangrams: %d", userDefaults[pangramCountKey] ?? 0)
            }
         } catch {
            NSLog("Error saving managed object context", error.localizedDescription)
            // TODO: send error event
            objectContext.undo()
            errored = true
         }
      } else {
         entryNotAcceptedEvent.send()
      }
      if !errored {
         objectWillChange.send()
         progress.currentWord = ""
      }
   }
   
   /**
    Hear out one of the strangest caveats I've ever seen as a software engineer: the parent view doesn't automatically detect
    that this game has been modified/saved -- apparently, the kicker is that *no Game attribute has been mutated, just attributes on its **progress** relationship*
    I've researched quite a few complaints with the same issue, e.g.:
    https://stackoverflow.com/questions/65171970/view-with-fetchrequest-doesnt-update-on-change
    https://forums.developer.apple.com/forums/thread/131231
    This does not seem to be caused by using the wrong MOC or of StateObject vs ObservedObject (I tried changing this class
    to the latter). It also isn't solved by enabling relationship prefetching in the fetch request (I really thought that might fix).
    */
   func closeGame() {
      game.dirtyTrigger += 1
      game.dirtyTrigger %= 100
   }
   
   private func scoreWord(_ word: String) -> Int16 {
      var score = Int16(word.count == 4 ? 1 : word.count)
      if game.isPangram(word: word) {
         score += 7
      }
      return score
   }
}
