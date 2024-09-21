//
//  Previews.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import Foundation

#if DEBUG
enum GamePreview {
   static func toSep_9_2024(_  game: Game) {
      game.date = Date(timeIntervalSince1970: 1725854400)
      game.centerLetterCode = Int32("e".unicodeScalars.first!.value)
      game.otherLetters = "yatpcf"
      game.allowedWords = ["accept", "acetate", "affect", "cafe", "cape", "effect", "face", "facet", "fate", "feet", "pace"]
      game.geniusScore = 89
      game.maximumScore = 127
   }
}

#endif
