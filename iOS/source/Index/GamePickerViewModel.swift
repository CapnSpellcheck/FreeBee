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
   @Published var showGameExistsDialog = false
   @Published var selectedDate: Date
   @Published private var latestAvailableDate: Date
      
   init(objectContext: NSManagedObjectContext) {
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
      PersistenceController.shared.container.newBackgroundContext()
   }
   
   func selectRandomDate() async {
      let backgroundObjectContext = PersistenceController.shared.container.newBackgroundContext()
      var randomDate: Date?
      var calendarAtUTC = Calendar.current
      calendarAtUTC.timeZone = TimeZone(abbreviation: "UTC")!

      repeat {
         let range = latestAvailableDate.timeIntervalSinceReferenceDate - kAug_4_2018.timeIntervalSinceReferenceDate
         let randomInterval = TimeInterval.random(in: 0...range)
         randomDate = Date(timeInterval: randomInterval, since: kAug_4_2018)
         NSLog("range: %f, randomInterval: %f, randomDate: %@", range, randomInterval, randomDate! as NSDate)
         let components = calendarAtUTC.dateComponents([.year, .month, .day], from: randomDate!)
         randomDate = calendarAtUTC.date(from: components)
         if randomDate != nil {
            NSLog("Selected random date since Aug 4 2018: %@", randomDate! as NSDate)
         }
      } while randomDate != nil && isGameLoaded(date: randomDate!, objectContext: backgroundObjectContext)
      if let randomDate {
         await Task {@MainActor in
            selectedDate = randomDate
            // wait for task to finish
         }.value
      }
   }
   
   private func isGameLoaded(
      date: Date,
      objectContext: NSManagedObjectContext = PersistenceController.shared.container.viewContext
   ) -> Bool {
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
