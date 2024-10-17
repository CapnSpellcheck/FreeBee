//
//  GameViewModelTest.swift
//  Free BeeTests
//
//  Created by G on 10/17/24.
//

import XCTest
@testable import Free_Bee

final class GameViewModelTest: XCTestCase {
   var persistenceController: PersistenceController!
   var game: Game!
   var viewModel: GameViewModel!
   
   override func setUpWithError() throws {
      // Put setup code here. This method is called before the invocation of each test method in the class.
      persistenceController = PersistenceController(inMemory: true)
      game = Game(context: persistenceController.container.viewContext)
      game.progress = GameProgress(context: game.managedObjectContext!)
      game.centerLetterCode = Int32("z".unicodeScalars.first!.value)
      game.otherLetters = "abcdef"
      game.geniusScore = 100
      game.maximumScore = 200
      game.date = Date(timeIntervalSinceReferenceDate: 0)
      game.allowedWords = ["abcd", "abcdefz"]
      viewModel = GameViewModel(game: game, objectContext: persistenceController.container.viewContext)
   }
   override func tearDownWithError() throws {
      // Put teardown code here. This method is called after the invocation of each test method in the class.
   }

   func test_enteredWordSummary() {
      XCTAssertEqual("No words yet", viewModel.enteredWordSummary)
      
      game.progress!.enteredWords = NSOrderedSet(array: ["abcd", "bcde"].map {
         EnteredWord(context: game.managedObjectContext!, string: $0)
      })
      XCTAssertEqual("Abcd\u{2003}Bcde", viewModel.enteredWordSummary)
   }
   
   func test_enterEnabled() {
      XCTAssertFalse(viewModel.enterEnabled, "enter disabled: current word empty")
      
      game.progress!.currentWord = "abc"
      XCTAssertFalse(viewModel.enterEnabled, "enter disabled: current word too short")

      game.progress!.currentWord = "abcde"
      XCTAssertFalse(viewModel.enterEnabled, "enter disabled: current word missing center letter")
      
      game.progress!.currentWord = "abcz"
      XCTAssertTrue(viewModel.enterEnabled, "enter enabled")
      
   }

   func test_append() {
      viewModel.append(letter: Character("a"))
      XCTAssertEqual("a", game.progress?.currentWord, "current word is \"a\"")
      
      viewModel.append(letter: "z")
      XCTAssertEqual("az", game.progress?.currentWord, "current word is \"az\"")
      
      game.progress?.currentWord = "abbcbcbcbcbcbcbbcbcbcbcb"
      viewModel.append(letter: "z")
      XCTAssertEqual("abbcbcbcbcbcbcbbcbcbcbcb", game.progress?.currentWord, "doesn't add letter, word max length")
   }
   
   func test_backspace() {
      viewModel.backspace()
      XCTAssertEqual("", game.progress!.currentWord)
      
      game.progress!.currentWord = "abcdef"
      viewModel.backspace()
      XCTAssertEqual("abcde", game.progress!.currentWord, "backspace")
   }
   
   func test_enter() {
      game.progress?.currentWord = "abcde"
      viewModel.enter()
      XCTAssertEqual(0, game.progress!.score, "score - word not accepted")
      XCTAssertEqual(0, game.progress!.enteredWords!.count, "enteredWords - word not accepted")
      XCTAssertEqual("", game.progress!.currentWord, "enter - currentWord cleared")
      
      game.progress?.currentWord = "abcdefz"
      viewModel.enter()
      XCTAssertEqual(14, game.progress!.score, "score - word accepted (pangram)")
      XCTAssertEqual(1, game.progress!.enteredWords!.count, "enteredWords - word accepted")
      XCTAssertEqual("", game.progress!.currentWord, "enter - currentWord cleared")

      game.progress?.currentWord = "abcd"
      viewModel.enter()
      XCTAssertEqual(15, game.progress!.score, "score - word accepted")
      XCTAssertEqual(2, game.progress!.enteredWords!.count, "enteredWords - word accepted")
      XCTAssertEqual("", game.progress!.currentWord, "enter - currentWord cleared")

      game.progress?.currentWord = "abcd"
      viewModel.enter()
      XCTAssertEqual(15, game.progress!.score, "score - repeat word not accepted")
      XCTAssertEqual(2, game.progress!.enteredWords!.count, "enteredWords - repeat word not accepted")
   }
   
   func test_onCurrentWordChanged() {
      let expectation = XCTestExpectation(description: "received onCurrentWordChanged")
      viewModel.onCurrentWordChanged = { word in
         XCTAssertEqual("abcd", word, "block param matches")
         expectation.fulfill()
      }
      
      game.progress!.currentWord = "abcd"
      wait(for: [expectation], timeout: 1)
   }
   
   func test_entryNotAcceptedEvent() {
      let expectation = XCTestExpectation(description: "received entryNotAcceptedEvent")
      let cancellable = viewModel.entryNotAcceptedEvent.sink {
         expectation.fulfill()
      }
      
      viewModel.enter()
      wait(for: [expectation], timeout: 1)
   }
}
