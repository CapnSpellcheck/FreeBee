//
//  GameList.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import SwiftUI
import CoreData

struct GameList: View {
   @Environment(\.managedObjectContext) private var viewContext

   @FetchRequest(
      sortDescriptors: [NSSortDescriptor(keyPath: \Game.date, ascending: false)],
      animation: .default)
   private var gameResults: FetchedResults<Game>
//   @FetchRequest(fetchRequest: Self.fetchRequest) var gameResults: FetchedResults<Game>

   var body: some View {
      NavigationView {
         List {
            Section("In progress") {
               ForEach(gameResults) { game in
                  NavigationLink(destination: {
                     GameView(game: game, context: viewContext)
                  }, label: {
                     HStack {
                        Text(game.date!, formatter: gameDateDisplayFormatter)
                        Spacer()
                        gameLettersText(game: game)
                        Spacer()
                        Text("Score: \(game.progress!.score)")
                     }
                  })
               }
            }
            Section("Start a new game") {
               Group {
                  Button("Choose a date…") {
                     ()
                  }
                  Button("Open a random game") {
                     ()
                  }
               }
               .foregroundColor(.primary)
            }
         }
         .listStyle(.grouped)
         .toolbar {
//            ToolbarItem {
//               Button(action: addGame) {
//                  Label("Add Game", systemImage: "plus")
//               }
//            }
         }
         .navigationTitle("Games")
      }
      .onAppear {
         viewContext.processPendingChanges()
      }
   }
   
   func gameLettersText(game: Game) -> some View {
      var string = AttributedString(game.allLetters)
      let centerIndex = string.index(string.startIndex, offsetByCharacters: 3)
      string.font = .body
      string.tracking = 1
      string[centerIndex..<string.index(centerIndex, offsetByCharacters: 1)]
         .foregroundColor = Color.accentColor
      return Text(string)
         .bold()
         .textCase(.uppercase)
   }
   
   static private var fetchRequest: NSFetchRequest<Game> {
      let request = NSFetchRequest<Game>(entityName: String(describing: Game.self))
      request.sortDescriptors = [
         NSSortDescriptor(
            keyPath: \Game.date,
            ascending: false)]
      request.relationshipKeyPathsForPrefetching = ["progress"]
      return request
   }
}

struct GameList_Previews: PreviewProvider {
   static var previews: some View {
      GameList().environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
   }
}
