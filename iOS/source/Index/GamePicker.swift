//
//  GamePicker.swift
//  Free Bee
//
//  Created by G on 9/25/24.
//

import SwiftUI
import CoreData

struct GamePicker: View {
   @EnvironmentObject private var router: Router
   @StateObject var viewModel: GamePickerViewModel
   
   init() {
      let model = GamePickerViewModel()
      _viewModel = StateObject(wrappedValue: model)
   }
   
   var body: some View {
      VStack {
         Text("Games are created by the New York Times. Choose a date to open the game from that date.")
         HStack(spacing: 16) {
            DatePickerUTC(
               selection: $viewModel.selectedDate,
               earliestDate: .constant(viewModel.earliestDate),
               latestDate: Binding($viewModel.latestAvailableDate)
            )
            Button("Go!") {
               viewModel.checkGameExists()
               if !viewModel.showGameExistsDialog {
                  loadSelectedDate()
               }
            }
            .buttonStyle(.borderedProminent)
            .foregroundColor(.primary)
         }
         Text("At this time, we can't preview the game for you before you open it. Maybe in the future!")
            .font(.footnote)
            .padding(.bottom, 40)
            .padding(.top, 8)
         Text("Or…")
            .font(.headline)
         Button("Open a random date") {
            Task {
               await viewModel.selectRandomDate()
               loadSelectedDate()
            }
         }
         .buttonStyle(.bordered)
         .foregroundColor(.primary)
      }
      .disabled(viewModel.isSearchingForDate)
      .navigationTitle("Choose a Game")
      .padding(.horizontal, 12)
      .confirmationDialog(
         "Game already in progress",
         isPresented: $viewModel.showGameExistsDialog,
         titleVisibility: .visible,
         actions: {
            Button("Cancel", role: .cancel) {}
            Button("Continue") {
               loadSelectedDate()
            }
         }
      ) {
         Text("Open this game with your existing progress?")
      }
   }
   
   @MainActor private func loadSelectedDate() {
      router.showGameLoader(date: viewModel.selectedDate)
   }
}

#if DEBUG
struct GamePicker_Previews: PreviewProvider {
   static var previews: some View {
      let context = PersistenceController.preview.container.viewContext
      NavigationView {
         GamePicker()
      }
   }
}
#endif
