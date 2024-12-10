//
//  DateExtension.swift
//  iosApp
//
//  Created by G on 12/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import ComposeApp

extension Date {
   func toInstant() -> Instant {
      let ms = Int64(timeIntervalSince1970)*1000
      return Instant.companion.fromEpochMilliseconds(epochMilliseconds: ms)
   }
}

/// Print the names for each method in a class
func printMethodNamesForClass(cls: AnyClass) {
   var methodCount: UInt32 = 0
   guard let methodList = class_copyMethodList(cls, &methodCount) else { return }
   for m in UnsafeBufferPointer(start: methodList, count: Int(methodCount)) {
      print(method_getName(m).description)
   }
   free(methodList)
}

/// Print the names for each method in a class with a specified name
func printMethodNamesForClassNamed(classname: String) {
   // NSClassFromString() is declared to return AnyClass!, but should be AnyClass?
   let maybeClass: AnyClass? = NSClassFromString(classname)
   if let cls: AnyClass = maybeClass {
      printMethodNamesForClass(cls: cls)
   }
   else {
      print("\(classname): no such class")
   }
}
