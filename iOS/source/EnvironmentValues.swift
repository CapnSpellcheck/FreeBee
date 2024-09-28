//
//  EnvironmentValues.swift
//  Free Bee
//
//  Created by G on 9/27/24.
//

import Foundation
import SwiftUI

extension EnvironmentValues {
   var navigationController: UINavigationController? {
      get { self[NavigationControllerKey.self] }
      set { self[NavigationControllerKey.self] = newValue }
   }
}

private struct NavigationControllerKey: EnvironmentKey {
    static let defaultValue: UINavigationController? = nil
}
 
