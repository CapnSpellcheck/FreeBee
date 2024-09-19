//
//  EnteredWordExtension.swift
//  Free Bee
//
//  Created by G on 9/18/24.
//

import Foundation
import CoreData

extension EnteredWord {
   convenience init(context: NSManagedObjectContext, string: String) {
      self.init(context: context)
      value = string
   }
}
