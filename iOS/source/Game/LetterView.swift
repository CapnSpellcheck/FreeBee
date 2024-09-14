//
//  LetterView.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import SwiftUI

struct LetterView: View {
   let letter: Character
   let isCenter: Bool
   let onTap: (Character) -> Void
   
   static let peripheralBackground = Color(white: 230.0/255)

   var body: some View {
      GeometryReader { proxy in
         let hexagon = HexagonShape()
         let background = isCenter ? .accentColor : Self.peripheralBackground
         Text(verbatim: String(letter))
            .font(.system(size: proxy.size.width / 3, weight: .medium))
            .frame(width: proxy.size.width, height: proxy.size.height)
            .background(background)
            .clipShape(hexagon)
            .contentShape(hexagon)
            .onTapGesture {
               onTap(letter)
            }
      }
   }
}

struct LetterView_Previews: PreviewProvider {
   struct ContainerView: View {
      @State var tapCount = 0
      
      var body: some View {
         VStack {
            LetterView(letter: "X", isCenter: true, onTap: { _ in
               tapCount += 1
            })
            .frame(width: 300, height: 300/1.1547)
            .background(.green)
            
            Text("Tap count: \(tapCount)")
         }
      }
   }
   static var previews: some View {
      ContainerView()
   }
}
