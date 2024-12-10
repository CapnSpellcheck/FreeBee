//
//  NavigationController.swift
//  iosApp
//
//  Created by G on 12/7/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import UIKit

class ComposeNavigationController: UINavigationController {
   override func viewDidLoad() {
      isNavigationBarHidden = true
   }
   
   // ComposeViewController doesn't currently have a way to control supportedInterfaceOrientations;
   // this is for the future if it does
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
