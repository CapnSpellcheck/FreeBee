//
//  GameProgressExtension.swift
//  Free Bee
//
//  Created by G on 9/16/24.
//

import Foundation

extension GameProgress {
   var currentWordDisplay: String {
      currentWord! + "_"
   }
   
   func hasEntered(word: String) -> Bool {
      enteredWords!.index { enteredWord, _, _ in
         (enteredWord as? EnteredWord)?.value == word
      } != NSNotFound
   }
}
