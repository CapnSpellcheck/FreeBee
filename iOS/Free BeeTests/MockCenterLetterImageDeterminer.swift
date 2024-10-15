//
//  MockCenterLetterImageDeterminer.swift
//  Free BeeTests
//
//  Created by G on 10/14/24.
//

import Foundation
@testable import Free_Bee

final class MockCenterLetterImageDeterminer : CenterLetterImageDeterminer {
   let value: Character?
   private(set) var called = false
   
   init(value: Character?) {
      self.value = value?.lowercased().first
   }
   
   func determine(gameDate: Date) async throws -> Character? {
      called = true
      return value
   }
   
}
