package com.letstwinkle.freebee.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.letstwinkle.freebee.compose.LetterHexagon
import com.letstwinkle.freebee.compose.polarOffset
import kotlin.math.PI
import kotlin.math.sqrt

private const val floatPi = PI.toFloat()

/**
 * The Honeycomb will fill the constrained width, put the desired padding in the modifier.
 */
@Composable fun LetterHoneycomb(
   centerLetter: Char,
   otherLetters: List<Char>,
   onLetterTap: (Char) -> Unit,
   modifier: Modifier
) {
   if (otherLetters.size != 6)
      throw AssertionError("A game must have 6 other letters")
   val hexPadding = 10.dp
   val modifier1 = modifier.fillMaxWidth().aspectRatio(0.6f*sqrt(3f)).testTag("honeycomb")
   
   BoxWithConstraints(modifier1, Alignment.Center) {
      val hexLength = ((if (maxWidth < maxHeight) maxWidth else maxHeight) - 4*hexPadding) / 3
      val hexHeightAndPadding = hexLength + hexPadding
      val baseModifier = Modifier.requiredSize(hexLength, hexLength) 
      LetterHexagon(centerLetter, true, onLetterTap, baseModifier)
      otherLetters.forEachIndexed { index, letter ->
         val theta = (index + 0.5f) * floatPi / 3
         LetterHexagon(
            letter,
            false,
            onLetterTap,
            baseModifier.polarOffset(hexHeightAndPadding, theta)
         )
      }
   }
}
