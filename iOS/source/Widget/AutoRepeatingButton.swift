//
//  AutoRepeatingButton.swift
//  Free Bee
//
//  Created by G on 9/16/24.
//

import SwiftUI

fileprivate let kFirstRepeatDelay = 0.5
fileprivate let kExtraRepeatDelay = 1.0 / 6

struct AutoRepeatingButton<L: View>: View {
   typealias Action = () -> Void
   typealias Label = () -> L
   
   private let action: Action
   private let label: Label
   
   @GestureState private var isBeingPressed = false
   
   init(action: @escaping Action, label: @escaping Label) {
      self.action = action
      self.label = label
   }
      
   var body: some View {
      Button(action: {}, label: label)
         .simultaneousGesture(
            LongPressGesture(minimumDuration: 9999, maximumDistance: 50)
               .updating($isBeingPressed) { currentState, gestureState, _ in
                  Task {@MainActor in
                     action()
                  }
                  gestureState = currentState
                  startRepeatTimer()
               }
         )
   }
   
   private func startRepeatTimer() {
      let startDate = Date(timeIntervalSinceNow: kFirstRepeatDelay)
      let timer = Timer(fire: startDate, interval: kExtraRepeatDelay, repeats: true) { timer in
         if isBeingPressed {
            action()
         } else {
            timer.invalidate()
         }
      }
      RunLoop.current.add(timer, forMode: .common)
   }
}

struct AutoRepeatingButton_Previews: PreviewProvider {
   struct ContainerView: View {
      @State var text = ""
      
      var body: some View {
         VStack {
            AutoRepeatingButton(action: {
               text.append("*")
            }, label: { Text("Button") })
            Text(text)
               .font(.system(size: 24, weight: .bold))
               .tracking(3)
         }
      }
   }
   static var previews: some View {
      ContainerView()
   }
}
