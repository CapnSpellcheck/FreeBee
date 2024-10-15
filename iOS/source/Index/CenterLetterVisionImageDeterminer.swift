//
//  CenterLetterVisionImageDeterminer.swift
//  Free Bee
//
//  Created by G on 10/14/24.
//

import Foundation
import Vision
import UIKit

struct CenterLetterVisionImageDeterminer: CenterLetterImageDeterminer {
   let dataFrom: DataFromProtocol
   
   func determine(gameDate: Date) async throws -> Character? {
      let yyyymmdd = gameDate.formatted(yyyymmddISO8601FormatStyle)
      let imageURL = URL(string: "https://nytbee.com/pics/\(yyyymmdd).png")!
      let (data, _) = try await dataFrom.data(from: imageURL)
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
         throw GameLoader.ParseError()
      }
      // Create a new image-request handler.
      let requestHandler = VNImageRequestHandler(cgImage: cgImage)
      // Create a new request to recognize text.
      let request = VNRecognizeTextRequest { _, _ in }
      request.revision = 2
      request.minimumTextHeight = 0.1
      try requestHandler.perform([request])
      
      guard let text = request.results?.first?.topCandidates(1).first?.string else {
         throw GameLoader.ParseError()
      }
      NSLog("determine(Date): text recognition: %@", text)
      return text.first?.lowercased().first
   }
   
}
