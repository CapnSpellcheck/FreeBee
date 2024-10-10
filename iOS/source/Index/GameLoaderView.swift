//
//  GameLoaderView.swift
//  Free Bee
//
//  Created by G on 9/27/24.
//

import SwiftUI

struct GameLoaderView: View {
   @Environment(\.navigationController) private var navController
   @State private var loader: GameLoader
   @State private var loadingStatus: GameLoader.Event?
   @State private var showErrorAlert = false
   
   init(gameDate: Date) {
      let loader = GameLoader(gameDate: gameDate)
      self.loader = loader
   }
   
   var body: some View {
      VStack {
         Text(statusText)
         ProgressView()
            .opacity(loadingStatus?.inProgress == true ? 1 : 0)
            .controlSize(.large)
      }
      .onReceive(loader.events) { event in
         loadingStatus = event
         if loadingStatusError != nil {
            showErrorAlert = true
         }
         if case .finished = loadingStatus {
            openGame()
         }
      }
      .navigationTitle(gameDateDisplayFormatter.string(from: loader.gameDate))
      .navigationBarTitleDisplayMode(.inline)
      .task {
         if loadingStatus == nil {
            Task.detached(priority: .high) {
               await loader.loadGame()
            }
         }
      }
      .alert(
         Text("Error"),
             isPresented: $showErrorAlert,
             presenting: loadingStatusError,
             actions: { _ in Button("OK") {
                navController?.popViewController(animated: true)
             } }
      ) { error in
         Text(error.localizedDescription)
      }
   }
   
   private var statusText: String {
      switch loadingStatus {
      case .downloading: return "Downloading…"
      case .parsing: return "Processing…"
      case .finished: return ""
      case .error: return "Failed"
      case .none: return ""
      }
   }
   
   private var loadingStatusError: Error? {
      if case .error(let e) = loadingStatus {
         return e
      }
      return nil
   }
   
   private func openGame() {
      if let gameView = GameView(
         gameDate: loader.gameDate,
         context: PersistenceController.shared.container.viewContext
      ) {
         navController?.replaceTopmost(with: gameView, animated: false, orientations: [.portrait])
      }
   }
}

struct GameLoaderView_Previews: PreviewProvider {
   static var previews: some View {
      GameLoaderView(gameDate: Date(timeIntervalSince1970: 1614902400))
   }
}
