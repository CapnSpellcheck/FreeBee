import Foundation
import Kanna

@objc public class KannaDocumentObjc : NSObject {
   let document: HTMLDocument
   @objc public init?(html: String) {
      if let document = try? HTML(html: html, encoding: .utf8) {
         self.document = document
      } else {
         return nil
      }
   }
   
   @objc public var rootElement: SearchableNodeObjc {
      SearchableNodeObjc(node: document)
   }
}

@objc public class SearchableNodeObjc : NSObject {
   let node: SearchableNode
   
  init(node: SearchableNode) {
      self.node = node
   }
   
   @objc public func xpathNodes(expr: String) -> SearchableNodeListObjc {
      switch node.xpath(expr) {
      case .NodeSet(let nodeset): return SearchableNodeListObjc(nodeset: nodeset)
      default: return SearchableNodeListObjc(nodeset: nil)
      }
   }
   
   @objc public func xpathNode(expr: String) -> SearchableNodeObjc? {
      node.at_xpath(expr).map { SearchableNodeObjc(node: $0) }
   }
   
   @objc public func xpathStr(expr: String) -> String {
      switch node.xpath(expr) {
      case .none: return ""
      case .NodeSet(let nodeset): return nodeset.text!
      case .Bool: return ""
      case .Number: return ""
      case .String(let text): return text
      }
   }
   
   @objc public func textContent() -> String {
      node.text ?? ""
   }
}

@objc public class SearchableNodeListObjc: NSObject {
   let nodeset: XMLNodeSet?
   
   init(nodeset: XMLNodeSet?) {
      self.nodeset = nodeset
   }
   
   @objc public var size: Int {
      nodeset?.count ?? 0
   }
   
   @objc public func item(i: Int) -> SearchableNodeObjc {
      SearchableNodeObjc(node: nodeset!.at(i)!)
   }
}
