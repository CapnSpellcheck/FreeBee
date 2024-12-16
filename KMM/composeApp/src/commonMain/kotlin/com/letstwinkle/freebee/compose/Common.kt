package com.letstwinkle.freebee.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.letstwinkle.freebee.*
import freebee.composeapp.generated.resources.Lexend
import freebee.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font
import org.lighthousegames.logging.logging
import kotlin.math.cos
import kotlin.math.sin

private val log = logging()

@Composable fun indentedDivider(startIndentation: Dp, backgroundColor: Color = Color.White) {
   Divider(
      Modifier.background(backgroundColor),
      color = rowDividerColor,
      startIndent = startIndentation
   )
}

@Composable fun MyAppTheme(content: @Composable () -> Unit) {
   val lightColors = lightColors(Color.White, secondary = yellowAccentColor, onPrimary = Color.Black)
   MaterialTheme(colors = lightColors) {
      CompositionLocalProvider(LocalIndication.provides(RipplelessPressIndication), content)
   }
}

enum class IconButtonPlacement(val pressOpacity: Float) {
   Toolbar(0.2f),
   // Inspection suggested that iOS uses 0.75 for buttons in main content. That doesn't seem like
   // enough press indication for me, so I'm trying a little stronger.
   Content(0.6f),
   ;
}

@Composable fun iOSStyleIconButton(
   onClick: () -> Unit,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
   placement: IconButtonPlacement = IconButtonPlacement.Toolbar,
   content: @Composable () -> Unit
) {
   val defaultAlpha = 1f
   var currentAlpha by remember { mutableStateOf(defaultAlpha) }
   Box(
      modifier = modifier
         .minimumInteractiveComponentSize()
         .clickable(
            onClick = onClick,
            enabled = enabled,
            role = Role.Button,
            interactionSource = interactionSource,
            indication = null
         )
         .pointerInput(Unit) {
            awaitEachGesture {
               log.d { "awaitEachGesture" }
               awaitFirstDown(true)
               log.d { "awaitFirstDown" }
               currentAlpha = placement.pressOpacity
               waitForUpOrCancellation()
               currentAlpha = defaultAlpha
            }
         },
      contentAlignment = Alignment.Center
   ) {
      val contentAlpha = if (enabled) currentAlpha else ContentAlpha.disabled
      CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = content)
   }
}

@Composable fun iOSStyleFilledButton(
   onClick: () -> Unit,
   modifier: Modifier = Modifier,
   enabled: Boolean = true,
   contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
   prominent: Boolean = false,
   content: @Composable RowScope.() -> Unit,
) {
      val backgroundColor = if (prominent) yellowAccentColor else secondaryFilledButtonColor
      Button(
         onClick,
         modifier,
         enabled,
         colors = ButtonDefaults.buttonColors(backgroundColor),
         contentPadding = contentPadding,
         elevation = flattenedButtonElevation(),
         content = content
      )
}

@Composable fun BlueIcon(
   painter: Painter,
   contentDescription: String?,
   modifier: Modifier = Modifier,
) {
   PressIcon(painter, contentDescription, iOSInspiredBlueActionColor, modifier)
}

@Composable fun AccentIcon(
   painter: Painter,
   contentDescription: String?,
   modifier: Modifier = Modifier,
) {
   PressIcon(painter, contentDescription, yellowAccentColor, modifier)
}

@Composable inline fun PressIcon(
   painter: Painter,
   contentDescription: String?,
   baseColor: Color,
   modifier: Modifier = Modifier,
) {
   Icon(painter, contentDescription, modifier, baseColor.copy(alpha = LocalContentAlpha.current))
}

@Composable inline fun gameLettersFontFamily() =
   if (LocalInspectionMode.current) FontFamily.Default else FontFamily(
      Font(Res.font.Lexend, weight = FontWeight.Medium),
      Font(Res.font.Lexend, weight = FontWeight.SemiBold),
   )

inline fun Modifier.polarOffset(r: Dp, theta: Float): Modifier =
   this.absoluteOffset(r * cos(theta), -r * sin(theta))

object RipplelessPressIndication : Indication {
   
   private class DefaultDebugIndicationInstance(
      private val isPressed: State<Boolean>,
      private val isHovered: State<Boolean>,
      private val isFocused: State<Boolean>,
   ) : IndicationInstance {
      override fun ContentDrawScope.drawIndication() {
         drawContent()
         if (isPressed.value) {
            drawRect(color = Color.Black.copy(alpha = 0.25f), size = size)
         } else if (isHovered.value || isFocused.value) {
            drawRect(color = Color.Black.copy(alpha = 0.1f), size = size)
         }
      }
   }
   
   @Composable
   override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
      val isPressed = interactionSource.collectIsPressedAsState()
      val isHovered = interactionSource.collectIsHoveredAsState()
      val isFocused = interactionSource.collectIsFocusedAsState()
      return remember(interactionSource) {
         DefaultDebugIndicationInstance(isPressed, isHovered, isFocused)
      }
   }
}

@Composable inline fun flattenedButtonElevation(): ButtonElevation =
   ButtonDefaults.elevation(pressedElevation = 2.dp, focusedElevation = 2.dp, hoveredElevation = 2.dp)
