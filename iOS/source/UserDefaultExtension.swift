//
//  UserDefaultExtension.swift
//  Free Bee
//
//  Created by G on 9/20/24.
//

import Foundation

extension UserDefaults {
   struct Key<T> {
      let name: String
      let type: T.Type
      init(type: T.Type, name: String) {
         self.type = type
         self.name = name
      }
   }
   
   static var pangramsCount: Key<Int> = .init(type: Int.self, name: "stats.pangrams_count")
   
   subscript<T>(key: Key<T>) -> T? {
     get { return value(for: key) }
     set { set(newValue, for: key) }
   }

   func value<T>(for key: Key<T>) -> T? {
     return value(forKey: key.name) as? T
   }

   func set<T>(_ value: T?, for key: Key<T>) {
     setValue(value, forKey: key.name)
   }

   func remove<T>(_ key: Key<T>) {
     removeObject(forKey: key.name)
   }
}
