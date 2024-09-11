//
//  Free_BeeApp.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import SwiftUI

@main
struct Free_BeeApp: App {
   let persistenceController = PersistenceController.shared
   
   var body: some Scene {
      WindowGroup {
         ContentView()
            .environment(\.managedObjectContext, persistenceController.container.viewContext)
      }
   }
}
