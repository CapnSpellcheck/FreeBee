//
//  ActionNavigationLink.swift
//  Free Bee
//
//  Created by G on 9/27/24.
//

import SwiftUI

// An estimate of the highlight duration of a tap on a List/TableView/CollectionView cell
fileprivate let kHighlightDuration = 0.6

public struct ActionNavigationLink<Content: View>: View {
  private let contentFactory: () -> Content
  private let action: () -> Void
  @State private var highlight = false

  public init(action: @escaping () -> Void, content contentFactory: @escaping () -> Content) {
    self.action = action
    self.contentFactory = contentFactory
  }
  
  public var body: some View {
    NavigationLink(destination: EmptyView(), label: contentFactory)
      .contentShape(Rectangle())
      .onTapGesture {
        highlight = true
        Task {
          try? await Task.sleep(nanoseconds: UInt64(kHighlightDuration * Double(NSEC_PER_SEC)))
          highlight = false
        }
        action()
      }
      .listRowBackground(highlight ? Color(UIColor.systemGray4) : Color(UIColor.systemBackground))
  }
}
