//
//  GameView.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import SwiftUI
import CoreData

fileprivate let kEntryNotAcceptedDuration: UInt64 = 2000
fileprivate let kGeneralHorizontalPadding = 16.0

struct GameView: View {
   @Environment(\.managedObjectContext) private var viewContext
   @StateObject var viewModel: GameViewModel
   @State private var showEntryNotAccepted = false
   
   init(game: Game, progress: GameProgress, context: NSManagedObjectContext) {
      _viewModel = StateObject(wrappedValue: GameViewModel(game: game, progress: progress, objectContext: context))
   }
   
   var body: some View {
      let game = viewModel.game
      let progress = viewModel.progress
      
      VStack(spacing: 0) {
         enteredWordList
         if showEntryNotAccepted {
            entryNotAcceptedMessage
         }
         Spacer(minLength: 12)
         Text(progress.currentWordDisplay)
            .tracking(2)
            .textCase(.uppercase)
            .lineLimit(1)
            .minimumScaleFactor(0.5)
            .font(.custom("HelveticaNeue-Medium", size: 28, relativeTo: .body))
            .dynamicTypeSize(...DynamicTypeSize.accessibility1)
            .padding(.horizontal, kGeneralHorizontalPadding)
         LetterHoneycomb(
            centerLetter: Character(game.centerLetter),
            otherLetters: Array(game.otherLetters!),
            letterTapped: viewModel.append(letter:)
         )
         editButtons
      }
      .onReceive(viewModel.entryNotAcceptedEvent) { _ in
         withAnimation {
            showEntryNotAccepted = true
            Task { @MainActor in
               try? await Task.sleep(nanoseconds: kEntryNotAcceptedDuration * NSEC_PER_MSEC)
               withAnimation {
                  showEntryNotAccepted = false
               }
            }
         }
      }
   }
   
   var entryNotAcceptedMessage: some View {
      HStack {
         Image(systemName: "x.circle.fill")
            .imageScale(.small)
            .foregroundColor(.red)
         Text("Entry not accepted")
      }
      .transition(.opacity)
   }
   
   var editButtons: some View {
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
   
   @ViewBuilder var enteredWordList: some View {
      HStack {
         Text(viewModel.enteredWordSummary)
            .lineLimit(1)
            .frame(maxWidth: .infinity)
         Spacer()
         Image(systemName: "chevron.down")
      }
      .padding(8)
      .overlay(RoundedRectangle(cornerRadius: 4).stroke(Color(UIColor.systemGray3), lineWidth: 1.5))
         }
      }
      .padding(.horizontal, kGeneralHorizontalPadding)
   }
}

struct GameView_Previews: PreviewProvider {
   static var previews: some View {
      let context = PersistenceController.preview.container.viewContext
      let game = Game(context: context)
      GamePreview.toSep_9_2024(game)
      let progress = GameProgress(context: context)
      progress.currentWord = "spelli"
      progress.enteredWords = NSOrderedSet(array: ["facet", "acetate", "peace", "ewfadafrdfs", "sdfgdsf"].map {
         EnteredWord(context: context, string: $0)
      })
      game.progress = progress
      return GameView(game: game, progress: progress, context: context)
         .environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
   }
}
