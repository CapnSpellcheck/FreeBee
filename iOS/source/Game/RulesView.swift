//
//  RulesView.swift
//  Free Bee
//
//  Created by G on 10/9/24.
//

import SwiftUI

private let headerIndent = 16.0
fileprivate let itemIndent = 32.0

struct RulesView: View {
   var body: some View {
      NavigationView {
         List {
            Section(content: {
               Group {
                  Text("Words must contain at least 4 letters.")
                  Text("Words must include the center letter.")
                  Text("Our word list does not include words that are obscure, hyphenated, or proper nouns.")
                  Text("No cussing either, sorry.")
                  Text("Letters can be used more than once.")
               }
               .listRowInsets(.init(top: 0, leading: itemIndent, bottom: 0, trailing: 0))
            }, header: {
               Text("Create words using letters from the hive").textCase(nil)
                  .listRowInsets(.init(top: 0, leading: headerIndent, bottom: 0, trailing: 0))
            })
            Section(content: {
               Group {
                  Text("4-letter words are worth 1 point each.")
                  Text("Longer words earn 1 point per letter.")
                  Text("Each puzzle includes at least one “pangram” which uses every letter. These are worth 7 extra points!")
               }
               .listRowInsets(.init(top: 0, leading: itemIndent, bottom: 0, trailing: 0))
            }, header: {
               Text("How to score").textCase(nil)
                  .listRowInsets(.init(top: 0, leading: headerIndent, bottom: 0, trailing: 0))
            })
         }
         .foregroundColor(.primary)
         .listStyle(.inset)
         .navigationTitle("Rules")
      }
   }
}

struct RulesView_Previews: PreviewProvider {
    static var previews: some View {
        RulesView()
    }
}
