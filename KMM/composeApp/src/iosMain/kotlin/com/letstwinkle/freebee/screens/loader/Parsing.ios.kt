package com.letstwinkle.freebee.screens.loader

// TODO
actual fun createHTMLDocument(html: String): HTMLDocument = object : HTMLDocument {
   private val dummyNode = object : Node {
      override fun xpathNodes(expr: String): NodeList = object : NodeList {
         override val size = 0
         
         override fun item(i: Int) = throw UnsupportedOperationException()
      }
      
      override fun xpathNode(expr: String): Node = this
      
      override fun xpathStr(expr: String): String = ""
      override fun textContent(): String = ""
      
   }
   override val rootElement: Node
      get() = dummyNode
}
