//
//  GamePickerViewModel.swift
//  Free Bee
//
//  Created by G on 9/25/24.
//

import Foundation
import CoreData
import Combine

fileprivate let kAug_4_2018: TimeInterval = 1533340800

class GamePickerViewModel: ObservableObject {
   let objectContext: NSManagedObjectContext
   
   @Published var showGameExistsDialog = false
      
   init(objectContext: NSManagedObjectContext) {
      self.objectContext = objectContext
   }

   var availableDateRange: ClosedRange<Date> {
      Date(timeIntervalSince1970: kAug_4_2018)...Date()
   }
      
   func checkGameExists(date: Date) {
      showGameExistsDialog = isGameLoaded(date: date)
   }
   
   private func isGameLoaded(date: Date) -> Bool {
      var components = Calendar.current.dateComponents([.day, .month, .year], from: date)
      components.hour = 0
      components.minute = 0
      components.second = 0
      
      var calendarAtUTC = Calendar.current
      calendarAtUTC.timeZone = TimeZone(abbreviation: "UTC")!
      
      guard let midnightDate = calendarAtUTC.date(from: components) else { return false }
      NSLog("Checking for existence of game with date=%@", midnightDate as NSDate)
      
      let fetchRequest = NSFetchRequest<Game>(entityName: String(describing: Game.self))
      fetchRequest.predicate = NSPredicate(format: "date == %@", midnightDate as NSDate)
      do {
         let count = try objectContext.count(for: fetchRequest)
         return count != 0
      } catch {
         NSLog("Couldn't check for existence of game. Error: %@", error.localizedDescription)
         return false
      }
   }
}
