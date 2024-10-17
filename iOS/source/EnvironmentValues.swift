//
//  EnvironmentValues.swift
//  Free Bee
//
//  Created by G on 9/27/24.
//

import Foundation
import SwiftUI

extension EnvironmentValues {
   var router: Router? {
      get { self[RouterKey.self] }
      set { self[RouterKey.self] = newValue }
   }
}

private struct RouterKey: EnvironmentKey {
   static let defaultValue: Router? = nil
}
