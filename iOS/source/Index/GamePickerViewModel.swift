//
//  GamePickerViewModel.swift
//  Free Bee
//
//  Created by G on 9/25/24.
//

import Foundation
import CoreData
import Combine

fileprivate let kAug_4_2018 = Date(timeIntervalSince1970: 1533340800)

class GamePickerViewModel: ObservableObject {
   let objectContext: NSManagedObjectContext
   
   @Published var showGameExistsDialog = false
   @Published var selectedDate: Date
   @Published private var latestAvailableDate: Date
      
   init(objectContext: NSManagedObjectContext) {
      self.objectContext = objectContext
      
      var calendarAtUTC = Calendar.current
      calendarAtUTC.timeZone = TimeZone(abbreviation: "UTC")!
      var components = Calendar.current.dateComponents([.year, .month, .day], from: Date())
      
      let todayGameDate = calendarAtUTC.date(from: components) ?? kAug_4_2018
      latestAvailableDate = todayGameDate
      selectedDate = todayGameDate
      determineLatestAvailableDate()
   }

   var availableDateRange: ClosedRange<Date> {
      kAug_4_2018...latestAvailableDate
   }
   
   func checkGameExists() {
      showGameExistsDialog = isGameLoaded(date: selectedDate)
   }
   
   private func isGameLoaded(date: Date) -> Bool {
      NSLog("Checking for existence of game with date=%@", date as NSDate)
      
      let fetchRequest = NSFetchRequest<Game>(entityName: String(describing: Game.self))
      fetchRequest.predicate = NSPredicate(format: "date == %@", date as NSDate)
      do {
         let count = try objectContext.count(for: fetchRequest)
         return count != 0
      } catch {
         NSLog("Couldn't check for existence of game. Error: %@", error.localizedDescription)
         return false
      }
   }
   
   private func determineLatestAvailableDate() {
//      let session = URLSession(configuration: .ephemeral)
   }
}
