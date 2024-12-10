//
//  SceneActivityType.swift
//  Free Bee
//
//  Created by G on 10/16/24.
//

import Foundation

private let base = "com.letstwinkle.freebee"

enum SceneActivity: String, CaseIterable {
   case gameIndex = "index"
   case gamePicker = "picker"
   case gameLoader = "loader"
   case game = "game"
   
   var activityType: String {
      "\(base)." + self.rawValue
   }
}

struct SceneActivityKeys {
   static let pickerDate = "pickerDate"
   static let gameDate = "date"
   static let gameEnteredLetters = "enteredLetters"
   static let gameURL = "url"
}
