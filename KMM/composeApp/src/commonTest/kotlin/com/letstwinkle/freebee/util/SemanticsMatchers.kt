package com.letstwinkle.freebee.util

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher

fun clickLabelMatcher(label: String): SemanticsMatcher = 
   SemanticsMatcher("Click label = $label") { node -> 
      node.config.getOrNull(SemanticsActions.OnClick)?.label == label 
   }
