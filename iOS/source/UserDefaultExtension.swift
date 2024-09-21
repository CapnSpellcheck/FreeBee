//
//  UserDefaultExtension.swift
//  Free Bee
//
//  Created by G on 9/20/24.
//

import Foundation

extension UserDefaults {
   enum Key<T>: String {
      case pangramsCount = "stats.pangrams_count"
   }
   
   subscript<T>(key: Key<T>) -> T? {
     get { return value(for: key) }
     set { set(newValue, for: key) }
   }

   func value<T>(for key: Key<T>) -> T? {
     return value(forKey: key.rawValue) as? T
   }

   func set<T>(_ value: T?, for key: Key<T>) {
     setValue(value, forKey: key.rawValue)
   }

   func remove<T>(_ key: Key<T>) {
     removeObject(forKey: key.rawValue)
   }
}
