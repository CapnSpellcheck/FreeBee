//
//  BytesForProtocol.swift
//  Free Bee
//
//  Created by G on 10/15/24.
//

import Foundation

protocol BytesForProtocol {
   associatedtype AsyncBytes: AsyncSequence
   func bytes(
      for request: URLRequest,
      delegate: URLSessionTaskDelegate?) async throws -> (AsyncBytes, URLResponse)
}
extension BytesForProtocol {
   func bytes(for request: URLRequest) async throws -> (AsyncBytes, URLResponse) {
      try await bytes(for: request, delegate: nil)
   }
}

extension URLSession: BytesForProtocol {}
