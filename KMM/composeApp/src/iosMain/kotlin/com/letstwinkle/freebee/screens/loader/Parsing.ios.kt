@file:OptIn(ExperimentalForeignApi::class)

package com.letstwinkle.freebee.screens.loader

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert

actual fun createHTMLDocument(html: String): HTMLDocument {
   val document = KannaDocumentObjc(html)
   return KannaDocumentWrapper(document)
}

private class KannaDocumentWrapper(private val kannaObjc: KannaDocumentObjc) : HTMLDocument {
   override val rootElement: Node
      get() = SearchableNodeWrapper(kannaObjc.rootElement())
}

private class SearchableNodeWrapper(private val searchableNode: SearchableNodeObjc) : Node {
   override fun xpathNodes(expr: String): NodeList = 
      SearchableNodeListWrapper(searchableNode.xpathNodesWithExpr(expr))
   
   override fun xpathNode(expr: String): Node? {
      val nodeListWrapper = xpathNodes(expr)
      return if (nodeListWrapper.size > 0) nodeListWrapper.item(0) else null
   }
   
   override fun xpathStr(expr: String): String = searchableNode.xpathStrWithExpr(expr)
   
   override fun textContent(): String = searchableNode.textContent()
}

private class SearchableNodeListWrapper(private val nodeList: SearchableNodeListObjc) : NodeList {
   override val size: Int
      get() = nodeList.size().convert()
   
   override fun item(i: Int): Node = SearchableNodeWrapper(nodeList.itemWithI(i.convert()))
}
