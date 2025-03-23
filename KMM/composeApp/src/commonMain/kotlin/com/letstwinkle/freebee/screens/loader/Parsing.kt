package com.letstwinkle.freebee.screens.loader

interface HTMLDocument {
   val rootElement: Node
}

interface Node {
   fun xpathNodes(expr: String): NodeList
   fun xpathNode(expr: String): Node?
   fun xpathStr(expr: String): String
   
   fun textContent(): String
}

interface NodeList : Iterable<Node> {
   val size: Int
   fun item(i: Int): Node
   
   override operator fun iterator() = object : Iterator<Node> {
      private var cur = 0
      override fun hasNext(): Boolean = cur < size
      
      override fun next(): Node = item(cur++)
   }
}

expect fun createHTMLDocument(html: String): HTMLDocument
