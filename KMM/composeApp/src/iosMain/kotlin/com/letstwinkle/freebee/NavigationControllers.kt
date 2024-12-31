package com.letstwinkle.freebee

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import platform.UIKit.*

@OptIn(BetaInteropApi::class)
class FreeBeeNavigationController : UINavigationController(null as ObjCClass?, null) {
   override fun viewDidLoad() {
      this.setNavigationBarHidden(true)
   }
   
   // TODO: can't implement 'supportedInterfaceOrientations' as an override. ask KMM team
}

fun UINavigationController.replaceTopmost(viewController: UIViewController, animated: Boolean = true) {
   setViewControllers(viewControllers.dropLast(1) + listOf(viewController), animated)
}
