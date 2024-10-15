//
//  DataFromProtocol.swift
//  Free Bee
//
//  Created by G on 10/14/24.
//

import Foundation

protocol DataFromProtocol {
   func data(
      from url: URL,
      delegate: URLSessionTaskDelegate?) async throws -> (Data, URLResponse)
}
extension DataFromProtocol {
   func data(from url: URL) async throws -> (Data, URLResponse) {
      try await data(from: url, delegate: nil)
   }
}

extension URLSession: DataFromProtocol {}
