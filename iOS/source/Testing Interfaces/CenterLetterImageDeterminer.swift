//
//  CenterLetterImageDeterminer.swift
//  Free BeeTests
//
//  Created by G on 10/14/24.
//

import Foundation

protocol CenterLetterImageDeterminer {
   func determine(gameDate: Date) async throws -> Character?
}
