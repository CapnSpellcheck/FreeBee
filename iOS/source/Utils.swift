//
//  Utils.swift
//  Free Bee
//
//  Created by G on 9/24/24.
//

import Foundation

let gameDateDisplayFormatter: DateFormatter = {
   let df = DateFormatter()
   df.locale = Locale(identifier: "en_US_POSIX")
   df.timeZone = TimeZone(abbreviation: "UTC")
   df.timeStyle = .none
   df.dateStyle = .medium
   return df
}()

extension String {
   func nilIfEmpty() -> String? {
      self == "" ? nil : self
   }
}
