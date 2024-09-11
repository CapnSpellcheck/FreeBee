//
//  GameView.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import SwiftUI

struct GameView: View {
   @Environment(\.managedObjectContext) private var viewContext
   let game: Game
   let progress: GameProgress
   
   var body: some View {
      EmptyView()
   }
}

struct GameView_Previews: PreviewProvider {
   static var previews: some View {
      let context = PersistenceController.preview.container.viewContext
      let game = Game(context: context)
      GamePreview.toSep_9_2024(game)
      let progress = GameProgress(context: context)
      game.progress = progress
      return GameView(game: game, progress: progress)
         .environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
   }
}
