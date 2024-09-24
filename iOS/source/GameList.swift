//
//  ContentView.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import SwiftUI
import CoreData

struct ContentView: View {
   @Environment(\.managedObjectContext) private var viewContext
   
   @FetchRequest(
      sortDescriptors: [NSSortDescriptor(keyPath: \Game.date, ascending: false)],
      animation: .default)
   private var gameResults: FetchedResults<Game>
   
   var body: some View {
      NavigationView {
         List {
            ForEach(gameResults) { game in
               NavigationLink(destination: {
                  GameView(game: game, context: viewContext)
               }, label: {
                  Text(game.date!, formatter: itemFormatter)
               })
            }
         }
         .toolbar {
            ToolbarItem {
               Button(action: addGame) {
                  Label("Add Game", systemImage: "plus")
               }
            }
         }
         .navigationTitle("Games")
         Text("Select an item")
      }
   }
   
   private func addGame() {
      withAnimation {
         let newGame = Game(context: viewContext)
         newGame.date = Date()
         
         do {
            try viewContext.save()
         } catch {
            // Replace this implementation with code to handle the error appropriately.
            // fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
            let nsError = error as NSError
            fatalError("Unresolved error \(nsError), \(nsError.userInfo)")
         }
      }
   }
}

private let itemFormatter: DateFormatter = {
   let formatter = DateFormatter()
   formatter.dateStyle = .short
   formatter.timeStyle = .medium
   return formatter
}()

struct ContentView_Previews: PreviewProvider {
   static var previews: some View {
      ContentView().environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
   }
}
