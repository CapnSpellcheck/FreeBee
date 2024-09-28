//
//  UINavigationController.swift
//  Free Bee
//
//  Created by G on 9/27/24.
//

import Foundation
import UIKit
import SwiftUI

extension UINavigationController {
   func replaceTopmost(with view: some View) {
      let newVC = UIHostingController(rootView: view.environment(\.navigationController, self))
      self.setViewControllers(viewControllers.dropLast() + [newVC], animated: true)
   }
   
   func push(view: some View) {
      let newVC = UIHostingController(rootView: view.environment(\.navigationController, self))
      pushViewController(newVC, animated: true)
   }
   
}
