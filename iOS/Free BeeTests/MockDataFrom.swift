//
//  MockDataFrom.swift
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
