//
//  InterfaceMocks.swift
//  Free BeeTests
//
//  Created by G on 10/14/24.
//

import Foundation
@testable import Free_Bee

struct MockDataFrom : DataFromProtocol {
   let errorToThrow: Error?
   let data: Data
   let response: URLResponse
   
   init(errorToThrow: Error?, data: Data = Data(), response: URLResponse = .init()) {
      self.errorToThrow = errorToThrow
      self.data = data
      self.response = response
   }
   
   func data(from url: URL, delegate: URLSessionTaskDelegate?) async throws -> (Data, URLResponse) {
      if let errorToThrow {
         throw errorToThrow
      }
      return (data, response)
   }
}

struct MockBytesFor : BytesForProtocol {
   typealias AsyncBytes = AsyncStream
   
   let errorToThrow: Error?
   let response: URLResponse
   
   init(errorToThrow: Error?, response: URLResponse = .init()) {
      self.errorToThrow = errorToThrow
      self.response = response
   }
   
   func bytes(for request: URLRequest, delegate: URLSessionTaskDelegate?) async throws -> (AsyncStream<Int>, URLResponse) {
      if let errorToThrow {
         throw errorToThrow
      }
      return (AsyncStream { cont in cont.finish() }, response)
   }
   
}
