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
import com.idapgroup.autosizetext.AutoSizeText
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.compose.AutoRepeatingIconButton
import com.letstwinkle.freebee.database.*
import io.woong.compose.grid.SimpleGridCells
import io.woong.compose.grid.VerticalGrid
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

private val log = logging()
const val entryNotAcceptedMessageVisibleDuration = 3000

@OptIn(ExperimentalMaterialApi::class)
@Composable fun GameScreen(
   game: IGame,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   MaterialTheme {
      val gameViewModel = GameViewModel(repository(), game.uniqueID)
      val dateString = formatGameDateToDisplay(game.date)
      val rulesState =
         rememberModalBottomSheetState(ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
      val coroutineScope = rememberCoroutineScope()
      
      Scaffold(topBar = {
         TopAppBar(
            title = { Text(dateString)},
            actions = {
               IconButton( { coroutineScope.launch { rulesState.show() } }) {
                  Icon(painterProvider.provide(PainterProvider.Resource.Rules), "rules")
               }
            })
      }) {
         GameWithSheets(gameViewModel, rulesState, painterProvider = painterProvider)
      }
   }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable fun <GameWithWords: IGameWithWords> GameWithSheets(
   viewModel: GameViewModel<GameWithWords>,
   rulesSheetState: ModalBottomSheetState,
   modifier: Modifier = Modifier,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   val enteredWordsSheetState = rememberModalBottomSheetState(
      ModalBottomSheetValue.Hidden,
      skipHalfExpanded = true,
   )
   
   ModalBottomSheetLayout(
      { RulesSheet() },
      sheetState = rulesSheetState,
      sheetShape = RoundedCornerShape(8.dp),
   ) {
      ModalBottomSheetLayout(
         { EnteredWordsSheet(viewModel.gameWithWords.value?.enteredWords.orEmpty().toList()) },
         sheetState = enteredWordsSheetState,
         sheetShape = RoundedCornerShape(8.dp),
         scrimColor = Color.Transparent
      ) {
         Game(viewModel, enteredWordsSheetState, modifier, painterProvider)
      }
   }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable fun <GameWithWords: IGameWithWords> Game(
   viewModel: GameViewModel<GameWithWords>,
   enteredWordsSheetState: ModalBottomSheetState,
   modifier: Modifier = Modifier,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   val coroutineScope = rememberCoroutineScope()
   val entryNotAcceptedAnimator = remember { Animatable(0f, Float.VectorConverter) }
   val entryNotAcceptedMessage = remember { mutableStateOf("") }
   val gameWithWords = viewModel.gameWithWords.value
   val isEnteredWordOverflow = remember { mutableStateOf(false) }
   
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
   
   Column(
      modifier.fillMaxSize().padding(vertical = 12.dp, horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
   ) {
      Row(
         Modifier.fillMaxWidth().padding(bottom = 16.dp),
         verticalAlignment = Alignment.CenterVertically
      ) {
         Text(
            scoreString(gameWithWords?.game?.score),
            Modifier.padding(end = 8.dp),
            style = bodyStyle
         )
         LinearProgressIndicator(viewModel.gameProgress, Modifier.height(2.dp).fillMaxWidth())
      }
      
      Row(
         Modifier.fillMaxWidth()
            .clickable(isEnteredWordOverflow.value) {
               coroutineScope.launch {
                  enteredWordsSheetState.show()
               }
            }
            .border(1.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
            .padding(8.dp),
         horizontalArrangement = Arrangement.SpaceBetween,
         verticalAlignment = Alignment.CenterVertically,
      ) {
         Text(
            viewModel.enteredWordSummary,
            Modifier.weight(1f).padding(end = 4.dp),
            overflow = TextOverflow.Ellipsis,
            color = viewModel.enteredWordSummaryColor,
            maxLines = 1,
            fontSize = 16.sp,
            onTextLayout = { layoutResult ->
               log.d { "hasVisualOverlow = ${layoutResult.hasVisualOverflow}" }
               isEnteredWordOverflow.value = layoutResult.hasVisualOverflow
            }
         )
         Image(
            painterProvider.provide(PainterProvider.Resource.ChevronDown),
            "toggle the bottom sheet that lists the words already entered",
            // SwiftUI magically chooses appropriate sizes for system images, this is what it comes up
            // with in the SwiftUI app
            Modifier.size(18.667.dp, 10.333.dp),
            alpha = if (isEnteredWordOverflow.value) 1f else 0f,
         )
      }
      
      Spacer(Modifier.weight(1f))
      
      Row(
         Modifier.padding(bottom = 16.dp).alpha(entryNotAcceptedAnimator.value),
         horizontalArrangement = Arrangement.spacedBy(8.dp),
         verticalAlignment = Alignment.CenterVertically
      ) {
         Image(
            painterProvider.provide(PainterProvider.Resource.XCircleFill),
            "entry not accepted",
            Modifier.size(16.dp, 16.dp),
            colorFilter = ColorFilter.tint(Color.Red)
         )
         Text(entryNotAcceptedMessage.value, style = subheadStyle)
      }
      
      val gameIsComplete = gameWithWords?.game?.isComplete != false
      
      if (!gameIsComplete) {
         AutoSizeText(
            gameWithWords!!.game.currentWordDisplay,
            maxLines = 1,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp
         )
      }
      
      // Temporary input mechanism for a word instead of the letter honeycomb
      TextField(gameWithWords?.game?.currentWord ?: "", singleLine = true, onValueChange = {
         viewModel.updateCurrentWord(it)
      })
      
      Row(
         Modifier.alpha(if (gameIsComplete) 0f else 1f),
         horizontalArrangement = Arrangement.spacedBy(44.dp)
      ) {
         AutoRepeatingIconButton({ viewModel.backspace() }, Modifier.size(48.dp, 48.dp)) {
            Icon(
               painterProvider.provide(PainterProvider.Resource.Backspace),
               "delete last letter",
               Modifier.requiredWidth(40.dp).requiredHeight(32.dp)
            )
         }
         IconButton({
            coroutineScope.launch {
               viewModel.enter()
            }
         }, Modifier.size(48.dp, 48.dp), enabled = viewModel.enterEnabled) {
            Icon(
               painterProvider.provide(PainterProvider.Resource.Enter),
               "submit the entered letters",
               Modifier.requiredWidth(46.dp).requiredHeight(32.dp)
            )
         }
      }
   }
}

@Composable
fun EnteredWordsSheet(words: List<IEnteredWord>) {
   log.d { "Entered words: $words" }
   Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
      Text("Entered words", style = headlineStyle, maxLines = 1)
      VerticalGrid(
         SimpleGridCells.Adaptive(100.dp),
         Modifier.padding(top = 12.dp)
      ) {
         val sortedWords = words.sortedBy { it.value }
            .map { it.value.replaceFirstChar(Char::titlecaseChar) }
         // convert from rows to columns
         for (word in sortedWords) {
            Text(word, Modifier.padding(bottom = 4.dp), style = bodyStyle)
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
