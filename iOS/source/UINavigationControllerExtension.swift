//
//  UINavigationController.swift
//  Free Bee
//
//  Created by G on 9/27/24.
//

import Foundation
import UIKit
import SwiftUI

class OrientationNavigationController: UINavigationController {
   override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
      topViewController?.supportedInterfaceOrientations ?? [.all]
   }
}

extension UINavigationController {
   func replaceTopmost(with view: some View, animated: Bool = true, orientations: UIInterfaceOrientationMask? = nil) {
      let newVC = OrientationHostingController(rootView: view.environment(\.navigationController, self))
      if let orientations {
         newVC.supportedOrientations = orientations
      }
      self.setViewControllers(viewControllers.dropLast() + [newVC], animated: animated)
   }
   
   func push(view: some View, orientations: UIInterfaceOrientationMask? = nil) {
      let newVC = OrientationHostingController(rootView: view.environment(\.navigationController, self))
      if let orientations {
         newVC.supportedOrientations = orientations
      }
      pushViewController(newVC, animated: true)
   }
   
}

fileprivate class OrientationHostingController<Content: View>: UIHostingController<Content> {
   var supportedOrientations: UIInterfaceOrientationMask = [.all]
   override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
      supportedOrientations
   }
}
