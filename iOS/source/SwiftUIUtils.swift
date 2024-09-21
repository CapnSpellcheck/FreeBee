//
//  SwiftUIUtils.swift
//  Free Bee
//
//  Created by G on 9/20/24.
//

import SwiftUI

@ViewBuilder func addOverlay(view: some View, alignment: Alignment = .center, condition: Bool, overlay: () -> some View) -> some View {
   if condition {
      view.overlay(alignment: alignment, content: overlay)
   } else {
      view
   }
}
