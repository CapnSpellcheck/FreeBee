//
//  EntityTest.swift
//  Free BeeTests
//
//  Created by G on 10/17/24.
//

import XCTest
@testable import Free_Bee

final class EntityTest: XCTestCase {
   var persistenceController: PersistenceController!
   var game: Game!
   
   override func setUpWithError() throws {
      // Put setup code here. This method is called before the invocation of each test method in the class.
      persistenceController = PersistenceController(inMemory: true)
      game = Game(context: persistenceController.container.viewContext)
      game.progress = GameProgress(context: persistenceController.container.viewContext)
      game.centerLetterCode = Int32("z".unicodeScalars.first!.value)
      game.otherLetters = "abcdef"
      game.geniusScore = 100
      game.maximumScore = 200
   }
   
   override func tearDownWithError() throws {
      // Put teardown code here. This method is called after the invocation of each test method in the class.
   }
   
   func testGame_isPangram() {
      XCTAssertFalse(game.isPangram(word: ""), "empty word is not pangram")
      XCTAssertFalse(game.isPangram(word: "abcdez"), "short word is not pangram")
      XCTAssertFalse(game.isPangram(word: "abcdef"), "short word is not pangram")
      XCTAssertTrue(game.isPangram(word: "fefdzabec"), "word is pangram")
   }
   
   func testGame_isGenius() {
      XCTAssertFalse(game.isGenius, "0 score is not genius")
      
      game.progress!.score = 99
      XCTAssertFalse(game.isGenius, "99 score is not genius")
      
      game.progress!.score = 100
      XCTAssertTrue(game.isGenius, "100 score is = genius")
      
      game.progress!.score = 999
      XCTAssertTrue(game.isGenius, "999 score is > genius")
   }
   
   func testGame_isComplete() {
      XCTAssertFalse(game.isComplete, "0 score is not complete")
      
      game.progress!.score = 199
      XCTAssertFalse(game.isComplete, "199 score is not complete")
      
      game.progress!.score = 200
      XCTAssertTrue(game.isComplete, "200 score is complete")
   }
   
   func testGameProgress_hasEnteredWord() {
      let progress = GameProgress(context: persistenceController.container.viewContext)
      progress.enteredWords = NSOrderedSet(array: ["abcd", "abcde", "abcdef", "abcdefz"].map {
         EnteredWord(context: persistenceController.container.viewContext, string: $0)
      })
      
      XCTAssertFalse(progress.hasEntered(word: "bcde"))
      XCTAssertFalse(progress.hasEntered(word: "abcdf"))
      XCTAssertTrue(progress.hasEntered(word: "abcde"))
      XCTAssertTrue(progress.hasEntered(word: "abcdefz"))
   }
}
