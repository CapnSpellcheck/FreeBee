//
//  GamePickerViewModelTest.swift
//  Free BeeTests
//
//  Created by G on 10/15/24.
//

import XCTest
@testable import Free_Bee

final class GamePickerViewModelTest: XCTestCase {
   var persistenceController: PersistenceController!
   
   override func setUpWithError() throws {
      persistenceController = PersistenceController(inMemory: true)
   }
   
   override func tearDownWithError() throws {
      // Put teardown code here. This method is called after the invocation of each test method in the class.
   }
   
   func test_isGameLoaded_true() {
      let response200 = HTTPURLResponse(url: URL(fileURLWithPath: "/"))
      let bytesFor = MockBytesFor(errorToThrow: nil, response: response200)
      let viewModel = GamePickerViewModel(persistenceController: persistenceController, dataFrom: bytesFor)
      let game = Game(context: persistenceController.container.viewContext)
      game.date = Date(timeIntervalSinceReferenceDate: 0)
      viewModel.selectedDate = game.date!
      
      viewModel.checkGameExists()
      XCTAssertTrue(viewModel.showGameExistsDialog, "game should exist")
   }
   
   func test_isGameLoaded_false() {
      let response404 = HTTPURLResponse(url: URL(fileURLWithPath: "/"), statusCode: 404, httpVersion: "1.1", headerFields: nil)!
      let bytesFor = MockBytesFor(errorToThrow: nil, response: response404)
      let viewModel = GamePickerViewModel(persistenceController: persistenceController, dataFrom: bytesFor)
      let game = Game(context: persistenceController.container.viewContext)
      game.date = Date(timeIntervalSinceReferenceDate: 0)
      viewModel.selectedDate = Date.now
      
      viewModel.checkGameExists()
      XCTAssertFalse(viewModel.showGameExistsDialog, "game should not exist")
   }
}
