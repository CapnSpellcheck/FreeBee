//
//  LetterView.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import SwiftUI

/*
 To make it a bit simple, and hide the fact that w > h for a regular hexagon, you should provide a
 square frame; LetterView will center the hexagon in the square and bleed outside it horizontally.
 */
struct LetterView: View {
   let letter: Character
   let isCenter: Bool
   let onTap: (Character) -> Void
   
   @GestureState var isDetectingLongPress = false
   
   var longPress: some Gesture {
      LongPressGesture(minimumDuration: 9999)
         .updating($isDetectingLongPress) { currentState, gestureState,
            _ in
            gestureState = currentState
//            transaction.animation = Animation.easeIn(duration: 2.0)
         }
   }
   
   static let peripheralBackground = Color(white: 230.0/255)

   var body: some View {
      GeometryReader { proxy in
         let hexagon = HexagonShape()
         let background = isCenter ? .accentColor : Self.peripheralBackground
         ZStack {
            Text(verbatim: String(letter))
               .font(.system(size: proxy.size.width / 3, weight: .medium))
         }
         .frame(width: 1.1547 * proxy.size.width, height: proxy.size.height)
         .background(background)
         .overlay {
            isDetectingLongPress ? Color.black.opacity(0.2) : Color.clear
         }
         .clipShape(hexagon)
         .contentShape(hexagon)
         .onTapGesture {
            onTap(letter)
         }
         .simultaneousGesture(longPress)
         .position(x: 0.5 * proxy.size.width, y: 0.5 * proxy.size.height)
      }
   }
}

struct LetterView_Previews: PreviewProvider {
   struct ContainerView: View {
      @State var tapCount = 0
      
      var body: some View {
         ZStack {
            Color.cyan
            VStack {
               LetterView(letter: "X", isCenter: true, onTap: { _ in
                  tapCount += 1
               })
               .frame(width: 300, height: 300)
               .background(.green)
               
               Text("Tap count: \(tapCount)")
            }
         }
      }
   }
   static var previews: some View {
      ContainerView()
   }
}
