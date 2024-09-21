//
//  Game.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import Foundation

extension Game {
   var centerLetter: UnicodeScalar {
      UnicodeScalar(Int(centerLetterCode))!
   }
   
   var centerLetterCharacter: Character {
      Character(centerLetter)
   }
   
   func isPangram(word: String) -> Bool {
      let wordLetters = Set(word)
      if word.count < 7 {
         return false
      }
      return wordLetters.contains(centerLetterCharacter) && wordLetters.isSuperset(of: otherLetters!)
   }
}
