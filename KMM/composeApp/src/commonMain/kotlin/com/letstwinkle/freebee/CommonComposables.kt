package com.letstwinkle.freebee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun indentedDivider(startIndentation: Dp, backgroundColor: Color = Color.White) {
   Divider(
      Modifier.background(backgroundColor),
      color = rowDividerColor,
      startIndent = startIndentation
   )
}
