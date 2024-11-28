package com.letstwinkle.freebee.compose

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.IconButton
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.*

@Composable fun AutoRepeatingIconButton(
   fireAction: () -> Unit,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   autoRepeatDelay: Int = 500,
   autoRepeatInterval: Int = 100,
   interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
   content: @Composable () -> Unit
) {
   val modifier1 = modifier.repeatingClickable(
      interactionSource,
      enabled,
      autoRepeatDelay.toLong(),
      autoRepeatInterval.toLong(),
      fireAction
   )
   IconButton({ }, modifier1, enabled, interactionSource, content)
}

@Composable private fun Modifier.repeatingClickable(
   interactionSource: MutableInteractionSource,
   enabled: Boolean,
   repeatDelay: Long,
   repeatInterval: Long,
   fireAction: () -> Unit
): Modifier = this.then(
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
   }.indication(
      interactionSource = interactionSource,
      indication = rememberRipple()
   )
)
