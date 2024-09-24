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
fileprivate let kHoneycombPadding = 16.0

struct GameView: View {
   @Environment(\.managedObjectContext) private var viewContext
   @StateObject var viewModel: GameViewModel
   @State private var showEntryNotAccepted = false
   @State private var expandEnteredWords = false
   
   init(game: Game, progress: GameProgress, context: NSManagedObjectContext) {
      let model = GameViewModel(game: game, progress: progress, objectContext: context)
      _viewModel = StateObject(wrappedValue: model)
   }
   
   var body: some View {
      let game = viewModel.game
      let progress = viewModel.progress
      
      VStack(spacing: 0) {
         scoreView
         enteredWordBar
         Spacer()
         if showEntryNotAccepted {
            entryNotAcceptedMessage
         }
         if !viewModel.gameComplete {
            Text(progress.currentWordDisplay)
               .tracking(2)
               .textCase(.uppercase)
               .lineLimit(1)
               .minimumScaleFactor(0.5)
               .font(.custom("HelveticaNeue-Medium", size: 28, relativeTo: .body))
               .dynamicTypeSize(...DynamicTypeSize.accessibility1)
               .padding(.horizontal, kGeneralHorizontalPadding)
         }
         addOverlay(
            view: LetterHoneycomb(
               centerLetter: game.centerLetterCharacter,
               otherLetters: Array(game.otherLetters!),
               letterTapped: viewModel.append(letter:)
            ).opacity(viewModel.gameComplete ? 0.4 : 1),
            condition: viewModel.gameComplete
         ) {
            Text("💯").font(.system(size: 150))
         }
         .disabled(viewModel.gameComplete)
         .padding(.vertical, kHoneycombPadding)
         editButtons
            .opacity(viewModel.gameComplete ? 0 : 1)
      }
      .onReceive(viewModel.entryNotAcceptedEvent) { _ in
         withAnimation(.linear) {
            showEntryNotAccepted = true
            Task { @MainActor in
               try? await Task.sleep(nanoseconds: NSEC_PER_MSEC*(kEntryNotAcceptedDuration + 350))
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
            .font(.system(.callout))
      }
      .transition(.opacity)
      .padding(.vertical, 12)
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
         .disabled(!viewModel.enterEnabled)
      }
      .buttonStyle(.plain)
      .font(.system(size: 36))
      .foregroundColor(.blue)
   }
   
   var enteredWordBar: some View {
      let wordBar = HStack {
         Text(viewModel.enteredWordSummary)
            .lineLimit(1)
         Spacer()
         Image(systemName: "chevron.down")
      }
         .zIndex(1)
         .padding(8)
         .overlay(RoundedRectangle(cornerRadius: 4)
            .stroke(Color(UIColor.systemGray3), lineWidth: 1.5))
         .onTapGesture {
            expandEnteredWords.toggle()
         }
      
      return addOverlay(view: wordBar, alignment: .bottomTrailing, condition: expandEnteredWords) {
         ScrollView {
            VStack(alignment: .leading, spacing: 6) {
               ForEach(viewModel.progress.enteredWords!.array as! Array<EnteredWord>, id: \.value) { enteredWord in
                  Text(enteredWord.value!.capitalized)
               }
            }
            .padding(12)
         }
         .background(Color(UIColor.systemGray6))
         .frame(maxHeight: 200)
         .fixedSize()
         .padding(.trailing, 8)
         .offset(y: 2)
         .alignmentGuide(.bottom, computeValue: { dimension in dimension[.top] })
      }
      .padding(.horizontal, kGeneralHorizontalPadding)
   }
   
   var scoreView: some View {
      HStack {
         Text(try! AttributedString(markdown: "Score:  **\(viewModel.progress.score)**"))
            .padding(.trailing, 8)
         let value = Float(viewModel.progress.score)
         let total = Float(viewModel.game.maximumScore)
         ProgressView(value: value, total: total)
            .scaleEffect(x: 1, y: 0.5)
      }
      .padding(.horizontal, kGeneralHorizontalPadding)
      .padding(.bottom, 16)
   }
}

struct GameView_Previews: PreviewProvider {
   static var previews: some View {
      let context = PersistenceController.preview.container.viewContext
      let game = Game(context: context)
      GamePreview.toSep_9_2024(game)
      let progress = GameProgress(context: context)
      progress.currentWord = "spelli"
      progress.enteredWords = NSOrderedSet(array: ["facet", "acetate", "peace", "effect", "accept"].map {
         EnteredWord(context: context, string: $0)
      })
      progress.score = game.maximumScore - 1
      game.progress = progress
      return GameView(game: game, progress: progress, context: context)
         .environment(\.managedObjectContext, PersistenceController.preview.container.viewContext)
   }
}
