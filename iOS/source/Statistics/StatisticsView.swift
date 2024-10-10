//
//  StatisticsView.swift
//  Free Bee
//
//  Created by G on 10/10/24.
//

import SwiftUI

struct StatisticsView: View {
   let statistics = StatisticsViewModel()
   
   var body: some View {
      List {
         HStack {
            Text("Games started")
            Spacer()
            Text(String(statistics.gamesStarted))
               .font(.custom("", size: 24, relativeTo: .body))
         }
         HStack {
            Text("Words played")
            Spacer()
            Text(String(statistics.wordsPlayed))
               .font(.custom("", size: 24, relativeTo: .body))
         }
         HStack {
            Text("Pangrams")
            Spacer()
            Text(String(statistics.pangramsPlayed))
               .font(.custom("", size: 24, relativeTo: .body))
         }
         HStack {
            Text("Genius reached")
            Spacer()
            Text(String(statistics.geniusGames))
               .font(.custom("", size: 24, relativeTo: .body))
         }
      }
      .listStyle(.plain)
      .navigationTitle("Statistics")
   }
}

struct StatisticsView_Previews: PreviewProvider {
   static var previews: some View {
      StatisticsView()
   }
}
