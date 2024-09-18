//
//  GameViewModel.swift
//  Free Bee
//
//  Created by G on 9/16/24.
//

import Foundation
import SwiftUI
import Combine

fileprivate let kMaxLetters = 19

final class GameViewModel: ObservableObject {
   let game: Game
   let progress: GameProgress
      
   init(game: Game, progress: GameProgress) {
      self.game = game
      self.progress = progress
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
      print(progress.currentWord!)
   }
   
   func enter() {
      
   }
}
