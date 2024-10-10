//
//  StatisticsViewModel.swift
//  Free Bee
//
//  Created by G on 10/10/24.
//

import Foundation
import CoreData

final class StatisticsViewModel {
   let gamesStarted: Int
   let wordsPlayed: Int
   let pangramsPlayed: Int
   let geniusGames: Int
   
   init(objectContext: NSManagedObjectContext = PersistenceController.shared.container.viewContext) {
      let wordRequest = NSFetchRequest<EnteredWord>(entityName: String(describing: EnteredWord.self))
      wordsPlayed = (try? objectContext.count(for: wordRequest)) ?? 0
      let progressRequest = NSFetchRequest<GameProgress>(entityName: String(describing: GameProgress.self))
      progressRequest.predicate = NSPredicate(format: "score > 0")
      gamesStarted = (try? objectContext.count(for: progressRequest)) ?? 0
      progressRequest.predicate = NSPredicate(format: "score >= game.geniusScore")
      geniusGames = (try? objectContext.count(for: progressRequest)) ?? 0

      pangramsPlayed = UserDefaults.standard[UserDefaults.pangramsCount] ?? 0
   }
}
