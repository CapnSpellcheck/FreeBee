//
//  GamePicker.swift
//  Free Bee
//
//  Created by G on 9/25/24.
//

import SwiftUI
import CoreData

struct GamePicker: View {
   @Environment(\.managedObjectContext) private var viewContext
   @StateObject var viewModel: GamePickerViewModel
   @State private var selectedDate = Date()
   
   init(context: NSManagedObjectContext) {
      let model = GamePickerViewModel(objectContext: context)
      _viewModel = StateObject(wrappedValue: model)
   }
   
   var body: some View {
      VStack {
         Text("Games are created by the New York Times. Choose a date to open the game from that date.")
         HStack(spacing: 16) {
            DatePicker("", selection: $selectedDate, in: viewModel.availableDateRange, displayedComponents: .date)
               .labelsHidden()
            Button("Go!") {
               viewModel.isGameLoaded(date: selectedDate)
            }
            .buttonStyle(.borderedProminent)
            .foregroundColor(.primary)
         }
         Text("At this time, we can't preview the game for you before you open it. Maybe in the future!")
            .font(.footnote)
            .padding(.bottom, 40)
      }
      .navigationTitle("Choose a Game")
      .padding(.horizontal, 12)
   }
}

struct GamePicker_Previews: PreviewProvider {
   static var previews: some View {
      let context = PersistenceController.preview.container.viewContext
      NavigationView {
         GamePicker(context: context)
      }
   }
}
