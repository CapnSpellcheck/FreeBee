//
//  GameLoaderTest.swift
//  Free BeeTests
//
//  Created by G on 9/10/24.
//

import XCTest
@testable import Free_Bee
import CoreData

extension HTTPURLResponse {
   convenience init(url: URL) {
      self.init(url: url, statusCode: 200, httpVersion: "1.1", headerFields: nil)!
   }
}

final class GameLoaderTest: XCTestCase {
   var persistenceController: PersistenceController!
   
   override func setUpWithError() throws {
      persistenceController = PersistenceController(inMemory: true)
   }
   
   override func tearDownWithError() throws {
      // Put teardown code here. This method is called after the invocation of each test method in the class.
   }
   
   func testOct_1_2024() async throws {
      let data = try! Data(contentsOf: Bundle(for: type(of: self)).url(forResource: "20241001_response", withExtension: "html")!)
      let date = Date(timeIntervalSince1970: 1727740800)
      let dataFrom = MockDataFrom(errorToThrow: nil, data: data, response: HTTPURLResponse(url: gameURL(forDate: date)!))
      let loader = GameLoader(
         gameDate: date,
         dataFrom: dataFrom,
         persistenceController: persistenceController
      )
      await loader.loadGame()
      
      let game: Game! = try! persistenceController.container.viewContext
         .fetch(NSFetchRequest<Game>(entityName: String(describing: Game.self))).first
      if game == nil {
         XCTFail("no game was inserted into the persistent store")
         return
      }
      
      XCTAssertEqual(UnicodeScalar(unicodeScalarLiteral: "i"), game.centerLetter, "correct center letter")
      XCTAssertEqual(144, game.geniusScore, "correct genius score")
      XCTAssertEqual(206, game.maximumScore, "correct maximum score")
      XCTAssertEqual(Set("VCTOLE".lowercased()), Set(game.otherLetters!.lowercased()), "correct other letters")
      let allowedWords: Set<String> = ["celli",
                                       "cite",
                                       "civet",
                                       "civic",
                                       "civil",
                                       "cocci",
                                       "coil",
                                       "colic",
                                       "collective",
                                       "collie",
                                       "cootie",
                                       "eclectic",
                                       "elective",
                                       "elicit",
                                       "elite",
                                       "evict",
                                       "evictee",
                                       "evil",
                                       "icicle",
                                       "illicit",
                                       "lice",
                                       "licit",
                                       "lilt",
                                       "lite",
                                       "little",
                                       "live",
                                       "loci",
                                       "oleic",
                                       "olio",
                                       "olive",
                                       "ollie",
                                       "tile",
                                       "till",
                                       "tilt",
                                       "title",
                                       "tittle",
                                       "toil",
                                       "toile",
                                       "toilet",
                                       "toilette",
                                       "veil",
                                       "vice",
                                       "vile",
                                       "viol",
                                       "violet",
                                       "voice",
                                       "voile",
                                       "votive"]
      XCTAssertEqual(allowedWords, game.allowedWords, "correct allowed words")
   }
   
   func testJuly_5_2020_usesCenterLetterImageDeterminer() async throws {
      let data = try! Data(contentsOf: Bundle(for: type(of: self)).url(forResource: "20200705_response", withExtension: "html")!)
      let date = Date(timeIntervalSince1970: 1593907200)
      let dataFrom = MockDataFrom(errorToThrow: nil, data: data, response: HTTPURLResponse(url: gameURL(forDate: date)!))
      let determiner: MockCenterLetterImageDeterminer = MockCenterLetterImageDeterminer(value: "I")
      let loader = GameLoader(
         gameDate: date,
         dataFrom: dataFrom,
         persistenceController: persistenceController,
         centerLetterDeterminer: determiner
      )
      await loader.loadGame()
      
      XCTAssertTrue(determiner.called, "used center letter determiner for July 5, 2020")
   }
}
