package com.letstwinkle.freebee.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.letstwinkle.freebee.rowDividerColor
import com.letstwinkle.freebee.yellowAccentColor
import kotlin.math.cos
import kotlin.math.sin

@Composable fun indentedDivider(startIndentation: Dp, backgroundColor: Color = Color.White) {
   Divider(
      Modifier.background(backgroundColor),
      color = rowDividerColor,
      startIndent = startIndentation
   )
}

@Composable fun MyAppTheme(content: @Composable () -> Unit) {
   val lightColors = lightColors(Color.White, secondary = yellowAccentColor)
   MaterialTheme(colors = lightColors, content = content)
}

inline fun Modifier.polarOffset(r: Dp, theta: Float): Modifier =
   this.absoluteOffset(r * cos(theta), -r * sin(theta))
