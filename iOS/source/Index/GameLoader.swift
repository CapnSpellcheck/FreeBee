//
//  GameLoader.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import Foundation
import Combine
import Kanna
import Vision
import UIKit


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
      guard let gameURL = gameURL(forDate: gameDate) else {
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
   
   private var gameImageURL: URL? {
      let yyyymmdd = gameDate.formatted(yyyymmddISO8601FormatStyle)
      return URL(string: "https://nytbee.com/pics/\(yyyymmdd).png")
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
         try await parseGame(document: document)
         let objectContext = PersistenceController.shared.container.viewContext
         try objectContext.performAndWait {
            try objectContext.save()
         }
         await sendEvent(.finished)
      } catch {
         await sendEvent(.error(error))
      }
   }
   
   private func parseGame(document: HTMLDocument) async throws {
      var result = document.xpath("//*[@id='main-answer-list'][1]/ul/li//text()[not(parent::a)]")
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
      
      if !determineLetters(words: allowedWords, centerLetter: &centerLetter, otherLetters: &otherLetters) {
         centerLetter = await determineCetterLetterByImage()
         guard centerLetter != nil else { return }
         // remove the center from otherLetters
         otherLetters = otherLetters?.filter { c in c != centerLetter! }
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
   
   private func determineLetters(
      words: Array<String>,
      centerLetter: inout Character?,
      otherLetters: inout String?
   ) -> Bool {
      var foundLetters = Set<Character>()
      var centerLetterCandidates = Set<Character>(words.first!)
      let words = words.shuffled()
      
      for word in words where foundLetters.count < 7 || centerLetterCandidates.count > 1 {
         let letters = Set(word)
         foundLetters.formUnion(letters)
         centerLetterCandidates.formIntersection(letters)
      }
      
      guard foundLetters.count == 7 && centerLetterCandidates.count == 1 else {
         otherLetters = String(foundLetters)
         return false
      }
      centerLetter = centerLetterCandidates.first
      _ = centerLetter.map { foundLetters.remove($0) }
      otherLetters = String(foundLetters)
      return true
   }
   
   private func determineCetterLetterByImage() async -> Character? {
      do {
         let (data, _) = try await urlSession.data(from: gameImageURL!)
         guard let cgImage = UIImage(data: data).flatMap({ image in
            let cgImage = image.cgImage!
            // Vision is very sensitive to the portion of the image chosen here. Not sure if it works
            // for all 26 letters.
            return cgImage.cropping(to: CGRect(
               x: 0.28*Double(cgImage.width),
               y: 0.28*Double(cgImage.height),
               width: 0.44*Double(cgImage.width),
               height: 0.44*Double(cgImage.height)))
         }) else {
            throw ParseError()
         }
         // Create a new image-request handler.
         let requestHandler = VNImageRequestHandler(cgImage: cgImage)
         // Create a new request to recognize text.
         let request = VNRecognizeTextRequest { _, _ in }
         request.revision = 2
         request.minimumTextHeight = 0.1
         try requestHandler.perform([request])

         guard let text = request.results?.first?.topCandidates(1).first?.string else {
            throw ParseError()
         }
         NSLog("determineCetterLetterByImage: text recognition: %@", text)
         return text.first?.lowercased().first
      } catch {
         await self.sendEvent(.error(error))
      }
      
      return nil
   }
   
   private func sendEvent(_ event: Event) async {
      NSLog("sendEvent: %@", String(describing: event))
      Task.detached {@MainActor in
         self.events.send(event)
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
