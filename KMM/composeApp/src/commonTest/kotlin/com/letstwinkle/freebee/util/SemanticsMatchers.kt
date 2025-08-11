package com.letstwinkle.freebee.util

import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*

fun clickLabelMatcher(label: String): SemanticsMatcher = 
   SemanticsMatcher("Click label = $label") { node -> 
      node.config.getOrNull(SemanticsActions.OnClick)?.label == label 
   }

fun isDisplayedMatcher(): SemanticsMatcher =
   SemanticsMatcher("Is displayed") { node ->
      fun isNotPlaced(node: LayoutInfo): Boolean {
         return !node.isPlaced
      }
      
      val layoutInfo = node.layoutInfo
      if (isNotPlaced(layoutInfo) || layoutInfo.findClosestParentNode(::isNotPlaced) != null) {
         return@SemanticsMatcher false
      }
      
      // check node doesn't clip unintentionally (e.g. row too small for content)
      val globalRect = node.boundsInWindow
      // skipped the check in 'AndroidAssertions.android.kt called `isInScreenBounds`
      
      return@SemanticsMatcher (globalRect.width > 0f && globalRect.height > 0f)
   }

inline fun invert(matcher: SemanticsMatcher) = SemanticsMatcher("NOT ${matcher.description}") {
   matcher.matches(it)
}

private fun LayoutInfo.findClosestParentNode(
   selector: (LayoutInfo) -> Boolean
): LayoutInfo? {
   var currentParent = this.parentInfo
   while (currentParent != null) {
      if (selector(currentParent)) {
         return currentParent
      } else {
         currentParent = currentParent.parentInfo
      }
   }
   
   return null
}
