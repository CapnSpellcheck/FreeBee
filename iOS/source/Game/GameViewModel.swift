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

final class GameViewModel: ObservableObject {
   let game: Game
   let progress: GameProgress
   let objectContext: NSManagedObjectContext
   let entryNotAcceptedEvent = PassthroughSubject<Void, Never>()
      
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
      if game.allowedWords?.contains(progress.currentWord!) == true {
         let enteredWord = EnteredWord(context: objectContext)
         enteredWord.value = progress.currentWord!
         progress.insertIntoEnteredWords(enteredWord, at: 0)
         do {
            try objectContext.save()
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
