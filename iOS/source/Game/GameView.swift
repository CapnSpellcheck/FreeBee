//
//  GameView.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import SwiftUI

struct GameView: View {
   @Environment(\.managedObjectContext) private var viewContext
   @StateObject var viewModel: GameViewModel
   
   init(game: Game, progress: GameProgress) {
      _viewModel = StateObject(wrappedValue: GameViewModel(game: game, progress: progress))
   }
   
   var body: some View {
      let game = viewModel.game
      let progress = viewModel.progress
      
      VStack(spacing: 0) {
         Text(progress.currentWordDisplay)
            .tracking(2)
            .textCase(.uppercase)
            .lineLimit(1)
            .minimumScaleFactor(0.5)
            .font(.custom("HelveticaNeue-Medium", size: 28, relativeTo: .body))
            .dynamicTypeSize(...DynamicTypeSize.accessibility1)
            .padding(.horizontal, 16)
         LetterHoneycomb(
            centerLetter: Character(game.centerLetter),
            otherLetters: Array(game.otherLetters!),
            letterTapped: viewModel.append(letter:)
         )
         HStack(spacing: 44) {
            AutoRepeatingButton(action: {
               viewModel.backspace()
            }, label: {
               Image(systemName: "delete.left.fill")
            })
            Button(action: {
               viewModel.enter()
            }, label: {
               if #available(iOS 16.0, *) {
                  Image(systemName: "return")
                     .fontWeight(.bold)
               } else {
                  Image(systemName: "return")
               }
            })
         }
         .font(.system(size: 36))
         .foregroundColor(.blue)
      }
   }
}

struct GameView_Previews: PreviewProvider {
   static var previews: some View {
      let context = PersistenceController.preview.container.viewContext
      let game = Game(context: context)
      GamePreview.toSep_9_2024(game)
      let progress = GameProgress(context: context)
      progress.currentWord = "SPELLI"
      game.progress = progress
      return GameView(game: game, progress: progress)
         .environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
   }
}
