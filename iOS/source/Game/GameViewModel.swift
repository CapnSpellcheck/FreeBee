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
   let progress: GameProgress
   let objectContext: NSManagedObjectContext
   let entryNotAcceptedEvent = PassthroughSubject<Void, Never>()
   

   private let userDefaults = UserDefaults.standard
      
   init(game: Game, progress: GameProgress, objectContext: NSManagedObjectContext) {
      self.game = game
      self.progress = progress
      self.objectContext = objectContext
   }

   var enteredWordSummary: String {
      (progress.enteredWords!.array as! Array<EnteredWord>).map {
         $0.value!.capitalized
      }.joined(separator: "\u{2003}")
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
         do {
            try objectContext.save()
            if game.isPangram(word: enteredWord) {
               NSLog("pangram: %@", enteredWord)
               userDefaults[.pangramsCount] = userDefaults[.pangramsCount] ?? 0 + 1
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
}
