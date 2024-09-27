//
//  GamePickerViewModel.swift
//  Free Bee
//
//  Created by G on 9/25/24.
//

import Foundation
import CoreData

class GamePickerViewModel: ObservableObject {
   let objectContext: NSManagedObjectContext
      
   init(objectContext: NSManagedObjectContext) {
      self.objectContext = objectContext
   }

   var availableDateRange: ClosedRange<Date> {
      Date(timeIntervalSince1970: 1533340800)...Date()
   }
   
   func isGameLoaded(date: Date) -> Bool {
      var calendarAtUTC = Calendar.current
      calendarAtUTC.timeZone = TimeZone(abbreviation: "UTC")!
      let findDate = {
         calendarAtUTC.date(bySettingHour: 0, minute: 0, second: 0, of: date, direction: .backward)
      }
      guard let midnightDate = findDate() else { return false }
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
