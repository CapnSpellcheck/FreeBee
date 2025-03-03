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
   @EnvironmentObject private var router: Router
   
   @FetchRequest(
      sortDescriptors: [NSSortDescriptor(keyPath: \Game.date, ascending: false)],
      animation: .default)
   private var gameResults: FetchedResults<Game>
   
   var body: some View {
      VStack {
         List {
            Section("In progress") {
               ForEach(gameResults) { game in
                  ActionNavigationLink(action: {
                     router.showGame(date: game.date!)
                  }, content: {
                     HStack {
                        Text(gameDateDisplayFormatter.string(from: game.date!))
                        Spacer()
                        gameLettersText(game: game)
                        Spacer()
                        scoreView(game: game)
                     }
                  })
               }
            }
            Section("Start a new game") {
               Group {
                  Button("Choose a new game") {
                     router.showGamePicker()
                  }
               }
               .foregroundColor(.primary)
            }
         }
         .listStyle(.grouped)
         .navigationTitle("Games")
         Spacer()
         Group {
            Text("Sponsor me")
               .font(.headline)
            Text("I appreciate tips! The only social payment platform I'm on is PayPal. Feel free to send me a gift.")
               .font(.callout)
            Button("Open PayPal") { }
               .foregroundColor(.blue)
         }
         .padding(.horizontal, 12)
         .padding(.bottom, 12)
      }
      .toolbar {
         ToolbarItem(id: "stats") {
            Button(action: {
               router.showStatistics()
            }, label: {
               Image(systemName: "chart.bar.xaxis")
            })
         }
      }
      .navigationBarTitleDisplayMode(.large)
   }
   
   func gameLettersText(game: Game) -> some View {
      var string = AttributedString(game.allLetters)
      let centerIndex = string.index(string.startIndex, offsetByCharacters: 3)
      string.font = .body
      string.tracking = 1.5
      string[centerIndex..<string.index(centerIndex, offsetByCharacters: 1)]
         .foregroundColor = Color.accentColor
      return Text(string)
         .bold()
         .textCase(.uppercase)
   }
   
   @ViewBuilder func scoreView(game: Game) -> some View {
      if game.isComplete {
         Text("💯")
      } else {
         Text("Score: \(game.progress?.score ?? 0)")
      }
   }
}

#if DEBUG
struct GameList_Previews: PreviewProvider {
   static var previews: some View {
      NavigationView {
         GameList().environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
      }
   }
}
#endif
