package com.letstwinkle.freebee.screens.loader

import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.w3c.dom.Document
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

actual fun createHTMLDocument(html: String): HTMLDocument {
   val soupDoc = Jsoup.parse(html)
   val jsoupW3Dom = W3CDom()
   jsoupW3Dom.namespaceAware(false)
   return W3CDocumentWrapper(jsoupW3Dom.fromJsoup(soupDoc))
}

private class W3CDocumentWrapper(private val document: Document) : HTMLDocument {
   private val xpath: XPath = XPathFactory.newInstance().newXPath()
   override val rootElement: Node = W3CNodeWrapper(document.documentElement)
   
   inner class W3CNodeWrapper(private val node: org.w3c.dom.Node) : Node {
      override fun xpathNodes(expr: String): NodeList {
         val w3cList = xpath.evaluate(expr, node, XPathConstants.NODESET) as org.w3c.dom.NodeList
         return W3CNodeListWrapper(w3cList)
      }
      
      override fun xpathNode(expr: String): Node {
         val w3cNode = xpath.evaluate(expr, node, XPathConstants.NODE) as org.w3c.dom.Node
         return W3CNodeWrapper(w3cNode)
      }
      
      override fun xpathStr(expr: String): String {
         return xpath.evaluate(expr, node, XPathConstants.STRING) as String
      }
      
      override fun textContent(): String = node.textContent
   }
   
   inner class W3CNodeListWrapper(private val nodeList: org.w3c.dom.NodeList): NodeList {
      override val size: Int
         get() = nodeList.length
      
      override fun item(i: Int): Node = W3CNodeWrapper(nodeList.item(i))
   }
   
}

