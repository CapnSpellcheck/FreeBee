//
//  Previews.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import Foundation
import CoreData

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
   
   static func toOct_22_2018(_ game: Game) {
      game.date = Date(timeIntervalSince1970: 1540180800)
      game.centerLetterCode = Int32("u".unicodeScalars.first!.value)
      game.otherLetters = "rlcayt"
      game.allowedWords = ["accrual", "accuracy", "actual", "actually", "actuary", "aura", "aural", "cull", "cult", "cultural", "culturally", "curl", "curly", "curry", "curt", "lull", "rural", "rutty", "tactual", "taut", "truly", "tutu", "yucca"]
      game.geniusScore = 113
      game.maximumScore = 161
   }
   
   static func toMar_5_2021(_ game: Game) {
      game.date = Date(timeIntervalSince1970: 1614920400)
      game.centerLetterCode = Int32("c".unicodeScalars.first!.value)
      game.otherLetters = "mihatr"
      game.allowedWords = ["acacia", "arch", "archaic", "arctic", "attach", "attic", "attract", "carat", "cart", "cataract", "catch", "cathartic", "chair", "charm", "chart", "chat", "chia", "chit", "chitchat", "citric", "cram", "critic", "hatch", "itch", "march", "match", "mimic", "rich", "tactic", "tract"]
      game.geniusScore = 186
      game.maximumScore = 266
   }
   
   // Seed canned games (above) into the persistent store if there are no Games in the store.
   static func seed(context: NSManagedObjectContext) {
      let countRequest = NSFetchRequest<Game>(entityName: String(describing: Game.self))
      guard (try? context.count(for: countRequest)) == 0 else { return }
      
      let sep9_2024 = Game(context: context)
      GamePreview.toSep_9_2024(sep9_2024)
      sep9_2024.progress = GameProgress(context: context)
      
      let oct22_2018 = Game(context: context)
      GamePreview.toOct_22_2018(oct22_2018)
      oct22_2018.progress = GameProgress(context: context)

      let mar5_2021 = Game(context: context)
      GamePreview.toMar_5_2021(mar5_2021)
      oct22_2018.progress = GameProgress(context: context)

      try? context.save()
   }
}

#endif
