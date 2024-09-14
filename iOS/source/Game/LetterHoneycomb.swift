//
//  LetterLayout.swift
//  Free Bee
//
//  Created by G on 9/13/24.
//

import SwiftUI

struct LetterHoneycomb/*<Content: View>*/: View {
   private let config: LetterHoneycombConfig

   init(
      centerLetter: Character,
      otherLetters: Array<Character>,
      letterTapped: @escaping (Character) -> Void
   ) {
      precondition(otherLetters.count == 6)
      config = LetterHoneycombConfig(
         centerLetter: centerLetter,
         otherLetters: otherLetters,
         letterTapped: letterTapped
      )
   }

   var body: some View {
      _VariadicView.Tree(LetterLayout(config: config)) {
         self
      }
   }
}

fileprivate struct LetterHoneycombConfig {
   let centerLetter: Character
   let otherLetters: Array<Character>
   let letterTapped: (Character) -> Void
}

// ignores any provided children, don't provide any
fileprivate struct LetterLayout: _VariadicView_UnaryViewRoot {
   let config: LetterHoneycombConfig
   
   @ViewBuilder func body(children: _VariadicView.Children) -> some View {
      let hexPadding = 10.0
      let tapWrapper: (Character) -> Void = { char in
         print("Letter tapped: ", String(char))
         config.letterTapped(char)
      }
      GeometryReader { proxy in
         // The height of the honeycomb is slightly greater than its width, namely: if s is the
         // side length of one of the hexagons, then w = 5s, but h = 3s√3; therefore, we calculate s
         // from the box height; the honeycomb will have horizontal padding even in a square box.
         let w = proxy.size.width
         let h = proxy.size.height
         let hexHeight = (min(w, h) - 4*hexPadding) / 3
         let hexHeightAndPadding = hexHeight + hexPadding

         LetterView(letter: config.centerLetter, isCenter: true, onTap: tapWrapper)
            .frame(width: hexHeight, height: hexHeight)
            .position(x: 0.5 * w, y: 0.5 * h)
         ForEach(Array(config.otherLetters.enumerated()), id: \.element)
         { i, letter in
            let theta = Double(i) * Double.pi / 3
            LetterView(letter: letter, isCenter: false, onTap: tapWrapper)
               .frame(width: hexHeight, height: hexHeight)
               .position(
                  x: 0.5*w + hexHeightAndPadding*sin(theta),
                  y: 0.5*h + hexHeightAndPadding*cos(theta)
               )
         }

      }
   }
}

struct LetterHoneycomb_Previews: PreviewProvider {
   static var previews: some View {
      let otherLetters = "123456".map { $0 }
      LetterHoneycomb(centerLetter: Character("A"), otherLetters: otherLetters) { _ in }
         .frame(width: 300, height: 300)
         .background(.green)
   }
}
