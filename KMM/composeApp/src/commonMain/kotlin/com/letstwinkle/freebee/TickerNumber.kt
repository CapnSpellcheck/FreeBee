/**
 * This is pretty much taken entirely from
 * https://github.com/esatgozcu/Compose-Rolling-Number, but that repo isn't KMP
 */
package com.letstwinkle.freebee

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable fun TickerNumberView(
   number: Int,
   modifier: Modifier = Modifier,
   prefix: String = "",
   suffix: String = "",
   animationDelayMsec: Int = 0,
   textStyle: TextStyle = TextStyle(fontSize = 24.sp)
) {
   SubcomposeLayout(modifier) { constraints ->
      val viewToMeasure = @Composable { Text(text = "8", style = textStyle) }
      
      val measuredWidth = subcompose("viewToMeasureWidth", viewToMeasure)[0]
         .measure(Constraints()).width.toDp()
      val measuredHeight = subcompose("viewToMeasureHeight", viewToMeasure)[0]
         .measure(Constraints()).height.toDp()
      val contentPlaceable = subcompose("content") {
         val result = number.toString().toCharArray()
         Column {
            //Avoid to use lazy row or column because we use repeat to add child view.
            //Otherwise you need to face this problem:
            //Error: Vertically scrollable component was measured with an infinity maximum height constraints, which is disallowed.
            //We want to use custom view inside column or row. If column or row has verticalScrollState, we will receive error.
            Row(modifier = Modifier
               .clip(RectangleShape)
               .layout { measurable, constraints ->
                  val placeable = measurable.measure(constraints)
                  layout(width = placeable.width, height = measuredHeight.roundToPx()) {
                     placeable.place(0, 0)
                  }
               }
               .wrapContentWidth()
            ) {
               if (prefix.isNotEmpty()) {
                  Text(text = prefix, style = textStyle)
               }
               repeat(result.size) {
                  NumberComponent(
                     char = result[it],
                     size = Size(width = measuredWidth.value, height = measuredHeight.value),
                     style = textStyle,
                     delayMS = animationDelayMsec,
                  )
               }
               if (suffix.isNotEmpty()) {
                  Text(text = suffix, style = textStyle)
               }
            }
         }
      }[0].measure(constraints)
      layout(contentPlaceable.width, contentPlaceable.height) {
         contentPlaceable.place(0, 0)
      }
   }
}

@Composable fun NumberComponent(char: Char, size: Size, style: TextStyle, delayMS: Int = 0) {
   if (char.isDigit()){
      DigitWheel(visibleNumber = char.digitToInt(), size, style, delayMS)
   }
}

@Composable private fun DigitWheel(visibleNumber: Int, size: Size, style: TextStyle, delayMS: Int){
   
   fun offset(): Float {
      val offsetMultiplier = 9f - visibleNumber.toFloat()
      val height = size.height
      return -height*offsetMultiplier
   }
   
   val itemTarget = remember { Animatable(offset()) }
   
   LaunchedEffect(visibleNumber){
      if (delayMS > 0)
         delay(delayMS.toDuration(DurationUnit.MILLISECONDS))
      itemTarget.animateTo(
         targetValue = offset(),
         animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
         ),
         initialVelocity = 2f
      )
   }
   
   Column(
      Modifier.offset(y = itemTarget.value.dp),
      horizontalAlignment = Alignment.CenterHorizontally
   ) {
      (9 downTo 0).forEach { digit ->
         DigitElement(number = digit, size, style)
      }
   }
}

@Composable private fun DigitElement(number: Int, size: Size, style: TextStyle){
   Text(
      text = "$number",
      modifier = Modifier.height(size.height.dp),
      style = style
   )
}