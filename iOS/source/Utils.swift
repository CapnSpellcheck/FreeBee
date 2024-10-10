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

let yyyymmddISO8601FormatStyle: Date.ISO8601FormatStyle =
   .iso8601.locale(Locale(identifier: "en_US_POSIX"))
   .dateSeparator(.omitted)
   .year().month().day()

func gameURL(forDate date: Date) -> URL? {
   let yyyymmdd = date.formatted(yyyymmddISO8601FormatStyle)
   return URL(string: "https://nytbee.com/Bee_\(yyyymmdd).html")
}

extension String {
   func nilIfEmpty() -> String? {
      self == "" ? nil : self
   }
}
