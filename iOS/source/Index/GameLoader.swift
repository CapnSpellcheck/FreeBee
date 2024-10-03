//
//  GameLoader.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import Foundation
import Combine
import Kanna

private let iso8601FormatStyle: Date.ISO8601FormatStyle =
   .iso8601.locale(Locale(identifier: "en_US_POSIX"))
   .dateSeparator(.omitted)
   .year().month().day()
private let urlSession: URLSession = {
   let configuration = URLSessionConfiguration.default
   configuration.httpShouldSetCookies = false
   configuration.urlCache = nil
   configuration.allowsConstrainedNetworkAccess = true
   return URLSession(configuration: configuration)
}()
private let regex = try! NSRegularExpression(pattern: "(\\d+)$")

private extension String {
   var entireRange: NSRange {
      NSRange(location: 0, length: self.count)
   }
}

class GameLoader {
   @MainActor let events = PassthroughSubject<Event, Never>()
   let gameDate: Date
   
   init(gameDate: Date) {
      self.gameDate = gameDate
   }
   
   func loadGame() async {
      NSLog("loadGame: date=%@", gameDate as NSDate)
      guard let gameURL = gameURL else {
         assertionFailure("GameLoader: gameURL was nil")
         return
      }
      await sendEvent(.downloading)
      NSLog("loadGame: URL=%@", gameURL.absoluteString)
      
      do {
         let (data, _) = try await urlSession.data(from: gameURL)
         await self.parseGame(htmlData: data)
      } catch {
         await self.sendEvent(.error(error))
      }
   }
   
   func isGameAvailable() {
      
   }
   
   private var gameURL: URL? {
      let yyyymmdd = gameDate.formatted(iso8601FormatStyle)
      return URL(string: "https://nytbee.com/Bee_\(yyyymmdd).html")
   }
   
   private func parseGame(htmlData: Data) async {
      await sendEvent(.parsing)
      let parseOption = ParseOption.htmlParseUseLibxml([.RECOVER, .NOERROR, .NOWARNING, .NONET, .NOBLANKS])
      // Note: NOBLANKS doesn't seem to work. Maybe options aren't working at all...
      guard let document = try? HTML(html: htmlData, encoding: .utf8, option: parseOption) else {
         await sendEvent(.error(ParseError()))
         return
      }
      
      do {
         try parseGame1(document: document)
      } catch {
         do {
            try parseGame2(document: document)
         } catch {
            await sendEvent(.error(error))
         }
      }

      do {
         let objectContext = PersistenceController.shared.container.viewContext
         try objectContext.performAndWait {
            try objectContext.save()
         }
         await sendEvent(.finished)
      } catch {
         await sendEvent(.error(error))
      }
   }
   
   private func parseGame1(document: HTMLDocument) throws {
      var result = document.xpath("//*[@id='main-answer-list'][1]/ul/li/div/text()")
      guard case .NodeSet(let answerNodes) = result else {
         throw ParseError()
      }
      let allowedWords = answerNodes.compactMap { e in
         e.text?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty()
      }
      guard !allowedWords.isEmpty else {
         throw ParseError()
      }
      result = document.xpath("//*[@id='puzzle-notes'][1]")
      guard case .NodeSet(let nodes) = result, let puzzleNotes = nodes.first else {
         throw ParseError()
      }
      result = puzzleNotes.xpath("//*[contains(., 'Maximum Puzzle Score')][1]")
      guard case .NodeSet(let nodes) = result,
            let text = nodes.first?.text,
            let match = regex.firstMatch(in: text, range: text.entireRange),
            let matchRange = Range(match.range, in: text),
            let maximumScore = Int16(text[matchRange])
      else {
         throw ParseError()
      }
      result = puzzleNotes.xpath("//*[contains(., 'Needed for Genius')][1]")
      guard case .NodeSet(let nodes) = result,
            let text = nodes.first?.text,
            let match = regex.firstMatch(in: text, range: text.entireRange),
            let matchRange = Range(match.range, in: text),
            let geniusScore = Int16(text[matchRange])
      else {
         throw ParseError()
      }
      
      var centerLetter: Character?
      var otherLetters: String?
      guard determineLetters(words: allowedWords, centerLetter: &centerLetter, otherLetters: &otherLetters) else {
         throw ParseError()
      }
      
      let objectContext = PersistenceController.shared.container.viewContext
      
      let game = Game(context: objectContext)
      game.date = gameDate
      game.allowedWords = Set(allowedWords)
      game.centerLetterCode = Int32(centerLetter!.unicodeScalars.first!.value)
      game.otherLetters = otherLetters
      game.geniusScore = geniusScore
      game.maximumScore = maximumScore
      game.progress = GameProgress(context: objectContext)

      NSLog("game parsed: %@", game)
   }
   
   private func parseGame2(document: HTMLDocument) throws {
      throw ParseError()
      // TODO
   }
   
   // This method assumes that the input words are a valid game; otherwise it may run indefinitely.
   private func determineLetters(
      words: Array<String>,
      centerLetter: inout Character?,
      otherLetters: inout String?
   ) -> Bool {
      var foundLetters = Set<Character>()
      var centerLetterCandidates = Set<Character>()
      repeat {
         let randomWord = words.randomElement()!
         let letters = Set(randomWord)
         foundLetters.formUnion(letters)
         if centerLetterCandidates.isEmpty {
            centerLetterCandidates.formUnion(letters)
         } else {
            centerLetterCandidates.formIntersection(letters)
         }
      } while foundLetters.count < 7 || centerLetterCandidates.count > 1
      
      centerLetter = centerLetterCandidates.first
      _ = centerLetter.map { foundLetters.remove($0) }
      otherLetters = String(foundLetters)
      return foundLetters.count == 6 && centerLetterCandidates.count == 1
   }
   
   private func sendEvent(_ event: Event) async {
      Task { @MainActor in
         events.send(event)
      }
   }
}

extension GameLoader {
   enum Event {
      case downloading
      case parsing
      case finished
      case error(Error)
      
      var inProgress: Bool {
         switch self {
         case .downloading, .parsing: return true
         default: return false
         }
      }
   }
   
   class ParseError: NSError {
      init() {
         super.init(domain: "freebee", code: 0)
      }
      required init?(coder: NSCoder) {
         fatalError("init(coder:) has not been implemented")
      }
      override var localizedDescription: String {
         "Unable to parse the downloaded game. The app scrapes a third party website to obtain game information; it may need an update to restore compatibility with that site."
      }
   }
   
   class DatabaseError: NSError {
      init() {
         super.init(domain: "freebee", code: 0)
      }
      required init?(coder: NSCoder) {
         fatalError("init(coder:) has not been implemented")
      }
      override var localizedDescription: String {
         "Database error."
      }
   }
}
