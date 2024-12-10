package com.letstwinkle.freebee.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.*

private val peripheralLetterBackground = Color(230f/255, 230f/255, 230f/255)

private typealias Point = Pair<Float, Float>

@Composable
fun LetterHexagon(letter: Char, isCenter: Boolean, onPress: (Char) -> Unit, modifier: Modifier = Modifier) {
   val modifier1 = modifier
      .aspectRatio(1.1547f, matchHeightConstraintsFirst = true)
      .clip(hexagonShape())
      .background(if (isCenter) MaterialTheme.colors.secondary else peripheralLetterBackground )
      .clickable { onPress(letter) }
   BoxWithConstraints(modifier1, contentAlignment = Alignment.Center) {
      Text(
         letter.uppercase(),
         fontSize = ((this.maxWidth.value / 3) / LocalDensity.current.fontScale).sp,
         fontFamily = gameLettersFontFamily(),
         color = Color.Black
      )
      
   }
}

fun hexagonShape(): Shape = GenericShape { size, _ ->
   val center = Point(0.5f*size.width, 0.5f*size.height)
   val radius = min(size.height, size.width) / 1.732f
   val vertices = (0..5).map { 
      val angle = it*PI.toFloat()/3
      Point(center.first + radius*cos(angle), center.second + radius*sin(angle))
   }
   moveTo(vertices[0].first, vertices[0].second)
   for (i in 1..<vertices.size) {
      lineTo(vertices[i].first, vertices[i].second)
   }
   close()
}
