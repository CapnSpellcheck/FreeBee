package com.letstwinkle.freebee.screens.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.database.IGame
import com.letstwinkle.freebee.database.IGameWithWords
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

private val log = logging()
const val entryNotAcceptedMessageVisibleDuration = 3000

@Composable fun GameScreen(
   game: IGame,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   MaterialTheme {
      val gameViewModel = GameViewModel(repository(), game.uniqueID)
      val dateString = formatGameDateToDisplay(game.date)
      
      Scaffold(topBar = {
         TopAppBar(
            title = { Text(dateString)},
            actions = {
            })
      }) {
         Game(gameViewModel, painterProvider = painterProvider)
      }
   }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun <GameWithWords: IGameWithWords> Game(
   viewModel: GameViewModel<GameWithWords>,
   modifier: Modifier = Modifier,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   val gameWithWords = viewModel.gameWithWords.value
   val coroutineScope = rememberCoroutineScope()
   val entryNotAcceptedAnimator = remember { Animatable(0f, Float.VectorConverter) }
   val entryNotAcceptedMessage = remember { mutableStateOf("") }
   
   LaunchedEffect(Unit) {
      for (message in viewModel.entryNotAcceptedEvents) {
         entryNotAcceptedMessage.value = message
         entryNotAcceptedAnimator.snapTo(1f)
         entryNotAcceptedAnimator.animateTo(
            0f, animationSpec = tween(
               delayMillis = entryNotAcceptedMessageVisibleDuration,
               easing = LinearEasing
            )
         )
      }
   }
   
   Column(modifier.fillMaxSize().padding(top = 12.dp).padding(horizontal = 16.dp)) {
      Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
         Text(
            scoreString(gameWithWords?.game?.score),
            Modifier.padding(end = 8.dp),
            style = bodyStyle
         )
         LinearProgressIndicator(viewModel.gameProgress, Modifier.height(2.dp).fillMaxWidth())
      }
      
      Row(
         Modifier.fillMaxWidth()
            .clickable {}
            .border(1.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
         verticalAlignment = Alignment.CenterVertically,
      ) {
         Text(
            viewModel.enteredWordSummary,
            overflow = TextOverflow.Ellipsis,
            color = viewModel.enteredWordSummaryColor,
            maxLines = 1,
            fontSize = 16.sp,
         )
         Image(
            painterProvider.provide(PainterProvider.Resource.ChevronDown),
            "toggle the bottom sheet listing the words already entered",
            // SwiftUI magically chooses appropriate sizes for system images, this is what it comes up
            // with in the SwiftUI app
            Modifier.size(18.667.dp, 10.333.dp)
         )
      }
      
      Spacer(Modifier.weight(1f))
      
      Row(
         Modifier.fillMaxWidth().padding(vertical = 12.dp).alpha(entryNotAcceptedAnimator.value),
         horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
         verticalAlignment = Alignment.CenterVertically
      ) {
         Image(
            painterProvider.provide(PainterProvider.Resource.XCircleFill),
            "entry not accepted",
            Modifier.size(15.667.dp, 15.667.dp),
            colorFilter = ColorFilter.tint(Color.Red)
         )
         Text(entryNotAcceptedMessage.value, style = bodyStyle)
      }
      
      // Temporary input mechanism for a word instead of the letter honeycomb
      TextField(gameWithWords?.game?.currentWord ?: "", singleLine = true, onValueChange = {
         log.d { "BasicTextField onValueChanged" }
         viewModel.updateCurrentWord(it)
      })
      
      Row(
         Modifier.fillMaxWidth(),
         horizontalArrangement = Arrangement.spacedBy(44.dp, Alignment.CenterHorizontally)
      ) {
         IconButton({ viewModel.backspace() }) {
            Icon(painterProvider.provide(PainterProvider.Resource.Backspace), "delete last letter")
         }
         TextButton({
            coroutineScope.launch {
               viewModel.enter()
            }
         }, enabled = viewModel.enterEnabled) {
            Text("Enter")
         }
      }
   }
}

private fun scoreString(score: Short?): AnnotatedString =
   buildAnnotatedString {
      append("Score:  ")
      score?.let {
         pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
         append(score.toString())
      }
   }
