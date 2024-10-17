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
   func replaceTopmost(
      with newViewController: UIViewController,
      animated: Bool = true
   ) {
      setViewControllers(viewControllers.dropLast() + [newViewController], animated: animated)
   }
   
}

