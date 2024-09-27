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
   
   var allLetters: String {
      otherLetters!.prefix(3) + String(centerLetterCharacter) + otherLetters!.suffix(3)
   }
   
   func isPangram(word: String) -> Bool {
      if word.count < 7 {
         return false
      }
      let wordLetters = Set(word)
      return wordLetters.contains(centerLetterCharacter) && wordLetters.isSuperset(of: otherLetters!)
   }
   
   var isGenius: Bool {
      progress!.score >= geniusScore
   }
   
   var isComplete: Bool {
      progress!.score >= maximumScore
   }
}
