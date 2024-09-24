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

   private let userDefaults = UserDefaults.standard
      
   init(game: Game, objectContext: NSManagedObjectContext) {
      self.game = game
      if game.progress == nil {
         game.progress = GameProgress(context: objectContext)
      }
      self.objectContext = objectContext
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
   
   var gameComplete: Bool {
      progress.score >= game.maximumScore
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
      if game.allowedWords?.contains(enteredWord) == true {
         progress.insertIntoEnteredWords(EnteredWord(context: objectContext, string: enteredWord), at: 0)
         progress.score += scoreWord(enteredWord)
         do {
            try objectContext.save()
            if game.isPangram(word: enteredWord) {
               NSLog("pangram: %@", enteredWord)
               let pangramCountKey = UserDefaults.pangramsCount
               userDefaults[pangramCountKey] = userDefaults[pangramCountKey] ?? 0 + 1
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
   
   private func scoreWord(_ word: String) -> Int16 {
      var score = Int16(word.count == 4 ? 1 : word.count)
      if game.isPangram(word: word) {
         score += 7
      }
      return score
   }
}
