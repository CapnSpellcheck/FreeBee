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
   
   init() {
#if DEBUG
      GamePreview.seed(context: persistenceController.container.viewContext)
#endif
   }
   
   var body: some Scene {
      WindowGroup {
         GameList()
            .environment(\.managedObjectContext, persistenceController.container.viewContext)
      }
   }
}
