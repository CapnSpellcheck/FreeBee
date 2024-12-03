package com.letstwinkle.freebee.compose

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.*

@Composable fun Modifier.autorepeatingClickable(
   interactionSource: MutableInteractionSource,
   enabled: Boolean = true,
   repeatDelay: Long = 500,
   repeatInterval: Long = 1000 / 6,
   fireAction: () -> Unit
): Modifier = composed {
   pointerInput(interactionSource, enabled) {
      coroutineScope {
         awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            // Create a down press interaction
            val downPress = PressInteraction.Press(down.position)
            val heldButtonJob = launch {
               // Send the press through the interaction source
               interactionSource.emit(downPress)
               var isFirstRepeat = true
               while (enabled && down.pressed) {
                  fireAction()
                  delay(if (isFirstRepeat) repeatDelay else repeatInterval)
                  isFirstRepeat = false
               }
            }
            val up = waitForUpOrCancellation()
            heldButtonJob.cancel()
            // Determine whether a cancel or release occurred, and create the interaction
            val releaseOrCancel = when (up) {
               null -> PressInteraction.Cancel(downPress)
               else -> PressInteraction.Release(downPress)
            }
            launch {
               // Send the result through the interaction source
               interactionSource.emit(releaseOrCancel)
            }
         }
      }
   }
}
