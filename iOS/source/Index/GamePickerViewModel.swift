//
//  GamePickerViewModel.swift
//  Free Bee
//
//  Created by G on 9/25/24.
//

import Foundation
import CoreData
import Combine

final class GamePickerViewModel: ObservableObject {
   @Published var showGameExistsDialog = false
   @Published var isSearchingForDate = false
   @Published var selectedDate: Date
   @Published var latestAvailableDate: Date

   let persistenceController: PersistenceController
   let dataFrom: any BytesForProtocol

   init(
      persistenceController: PersistenceController = PersistenceController.shared,
      dataFrom: any BytesForProtocol = URLSession(configuration: .ephemeral)
   ) {
      var calendarAtUTC = Calendar.current
      calendarAtUTC.timeZone = TimeZone(abbreviation: "UTC")!
      var components = Calendar.current.dateComponents([.year, .month, .day], from: Date())
      
      let todayGameDate = calendarAtUTC.date(from: components) ?? earliestDate
      latestAvailableDate = todayGameDate
      selectedDate = todayGameDate
      self.persistenceController = persistenceController
      self.dataFrom = dataFrom

      Task.detached(priority: .high) {
         await self.determineLatestAvailableDate(today: todayGameDate)
      }
   }

   let earliestDate = Date(timeIntervalSince1970: 1533340800)
   
   func checkGameExists() {
      showGameExistsDialog = isGameLoaded(
         date: selectedDate,
         objectContext: persistenceController.container.viewContext
      )
   }
   
   func selectRandomDate() async {
      await MainActor.run {
         isSearchingForDate = true
      }
      let backgroundObjectContext = persistenceController.container.newBackgroundContext()
      var randomDate: Date?
      var calendarAtUTC = Calendar.current
      calendarAtUTC.timeZone = TimeZone(abbreviation: "UTC")!

      repeat {
         let range = latestAvailableDate.timeIntervalSinceReferenceDate - earliestDate.timeIntervalSinceReferenceDate
         let randomInterval = TimeInterval.random(in: 0...range)
         randomDate = Date(timeInterval: randomInterval, since: earliestDate)
         NSLog("range: %f, randomInterval: %f, randomDate: %@", range, randomInterval, randomDate! as NSDate)
         let components = calendarAtUTC.dateComponents([.year, .month, .day], from: randomDate!)
         randomDate = calendarAtUTC.date(from: components)
         if randomDate != nil {
            NSLog("Selected random date since Aug 4 2018: %@", randomDate! as NSDate)
         }
      } while randomDate != nil && isGameLoaded(date: randomDate!, objectContext: backgroundObjectContext)
      if let randomDate {
         // wait for task to finish
         await MainActor.run {
            selectedDate = randomDate
         }
      }
      await MainActor.run {
         isSearchingForDate = false
      }
   }
   
   private func isGameLoaded(date: Date, objectContext: NSManagedObjectContext) -> Bool {
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
   
   // if the user is east of PST, "today's" game, determined by the device local time, may not
   // exist yet. In addition, the entry on nytbee.com may take longer.
   private func determineLatestAvailableDate(today: Date) async {
      let objectContext = persistenceController.container.newBackgroundContext()
      var checkDate = today
      
      while checkDate >= earliestDate {
         // if the game is saved locally, skip it
         if !isGameLoaded(date: checkDate, objectContext: objectContext) {
            // check whether the website responds with 404.
            if let gameURL = gameURL(forDate: checkDate)  {
               var request = URLRequest(url: gameURL)
               // NOTE: This really should work with a HEAD request, but it doesn't, at least in
               // my testing. I attempted to investigate this per https://forums.developer.apple.com/forums/thread/728371,
               // since `curl -I https://nytbee.com/Bee_20241019.html` works, but I haven't yet found
               // the cause.
//               request.httpMethod = "HEAD"
//               request.setValue("curl/8.7.1", forHTTPHeaderField: "user-agent")
               
               if let (_, response) = try? await dataFrom.bytes(for: request),
                  (response as! HTTPURLResponse).statusCode < 300 {
                  await MainActor.run { [checkDate] in
                     latestAvailableDate = checkDate
                  }
                  return
               }
            } else {
               assertionFailure("GamePickerViewModel: gameURL was nil")
            }
         }
         
         // update checkDate
         guard let newDate = Calendar.current.date(byAdding: .day, value: -1, to: checkDate) else {
            assertionFailure("GamePickerViewModel: unable to rewind date")
            return
         }
         checkDate = newDate
      }
   }
}
