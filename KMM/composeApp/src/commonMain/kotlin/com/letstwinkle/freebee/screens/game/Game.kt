package com.letstwinkle.freebee.screens.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.compose.*
import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.screens.BackNavigator
import io.woong.compose.grid.SimpleGridCells
import io.woong.compose.grid.VerticalGrid
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.lighthousegames.logging.logging

private val log = logging()
const val entryNotAcceptedMessageVisibleDuration = 3000

@Composable inline fun <Id> GameScreen(
   repository: FreeBeeRepository<Id, *, *>,
   game: IGame<Id>,
   backNavigator: BackNavigator,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   GameScreen(repository, game.uniqueID, game.date, backNavigator, painterProvider)
}

private val positionProvider = object : PopupPositionProvider {
   override fun calculatePosition(
      anchorBounds: IntRect,
      windowSize: IntSize,
      layoutDirection: LayoutDirection,
      popupContentSize: IntSize
   ) = anchorBounds.bottomCenter - IntOffset(popupContentSize.width/2, 0)
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable fun <Id> GameScreen(
   repository: FreeBeeRepository<Id, *, *>,
   gameID: Id,
   gameDate: LocalDate,
   backNavigator: BackNavigator,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   MyAppTheme {
      val viewModel = viewModel { GameViewModel(repository, gameID) }
      val rulesState =
         rememberModalBottomSheetState(ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
      val coroutineScope = rememberCoroutineScope()
      
      Scaffold(topBar = {
         CenterAlignedTopAppBar(
            title = { Text(formatGameDateToDisplay(gameDate)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            actions = {
               if (viewModel.gameWithWords.value?.game?.isGenius == true) {
                  val geniusPopupOpen = rememberSaveable { mutableStateOf(false) }
                  
                  Box {
                     iOSStyleIconButton({ geniusPopupOpen.value = true }) {
                        val brainPaint = painterProvider.provide(PainterProvider.Resource.Brain)
                        PressIcon(brainPaint, "you earned Genius", brainColor)
                     }
                     if (geniusPopupOpen.value) {
                        val props = PopupProperties(dismissOnBackPress = false)
                        Popup(positionProvider, { geniusPopupOpen.value = false }, properties = props) {
                           val geniusStr = "You reached genius level. Smarty!"
                           val popupShape = RoundedCornerShape(8.dp)
                           // TODO: find constant for elevation
                           Surface(Modifier.widthIn(max = 120.dp), elevation = 3.dp, shape = popupShape) {
                              Text(
                                 geniusStr,
                                 Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                 textAlign = TextAlign.Center,
                                 style = footnoteStyle
                              )
                           }
                        }
                     }
                  }
               }
               iOSStyleIconButton({ coroutineScope.launch { rulesState.show() } }) {
                  val rulesPaint = painterProvider.provide(PainterProvider.Resource.Rules)
                  AccentIcon(rulesPaint, "rules")
               }
            },
            navigationIcon = backNavigationButton(backNavigator::goBack),
         )
      }) {
         GameWithSheets(viewModel, rulesState, Modifier.padding(it), painterProvider)
      }
   }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable fun <Id, Game: IGame<Id>> GameWithSheets(
   viewModel: GameViewModel<Id, Game, *>,
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
      modifier, // NOTE: on iPhone this doesn't seem to inset for the bottom home gesture navigation
                // Could be an issue with applying window insets to MBSL
      sheetState = rulesSheetState,
      sheetShape = RoundedCornerShape(8.dp),
   ) {
      ModalBottomSheetLayout(
         { EnteredWordsSheet(viewModel.gameWithWords.value?.enteredWords.orEmpty().toList()) },
         sheetState = enteredWordsSheetState,
         sheetShape = RoundedCornerShape(8.dp),
         scrimColor = Color.Transparent
      ) {
         Game(viewModel, enteredWordsSheetState, painterProvider = painterProvider)
      }
   }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable private fun <Id, Game: IGame<Id>> Game(
   viewModel: GameViewModel<Id, Game, *>,
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
         launch {
            entryNotAcceptedAnimator.animateTo(
               0f, animationSpec = tween(
                  delayMillis = entryNotAcceptedMessageVisibleDuration,
                  easing = LinearEasing
               )
            )
         }
      }
   }
   
   Column(
      modifier.fillMaxSize().padding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom).asPaddingValues())
         .padding(vertical = 12.dp, horizontal = 16.dp),
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
         LinearProgressIndicator(
            viewModel.gameProgress,
            Modifier.height(2.dp).fillMaxWidth(),
            color = MaterialTheme.colors.secondary
         )
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
      
      AutoSizeText(
         viewModel.currentWordDisplay, 
         maxLines = 1,
         maxTextSize = 28.sp,
         fontWeight = FontWeight.SemiBold,
         fontFamily = gameLettersFontFamily(),
         letterSpacing = 2.sp,
      )
      
      val gameIsCompleteOrNull = gameWithWords?.game?.isComplete != false
      
      Box(contentAlignment = Alignment.Center) {
         gameWithWords?.let {
            LetterHoneycomb(
               gameWithWords.game.centerLetterCharacter,
               gameWithWords.game.otherLetters.toList(),
               viewModel::append,
               Modifier.padding(vertical = 16.dp)
                  .alpha(if (gameIsCompleteOrNull) 0.4f else 1f)
            )
         }
         if (gameIsCompleteOrNull && gameWithWords != null) {
            Text("\uD83D\uDCAF", fontSize = (150f / LocalDensity.current.fontScale).sp)
         }
      }
      
      Spacer(Modifier.weight(1f))
      
      Row(
         Modifier.alpha(if (gameIsCompleteOrNull) 0f else 1f).padding(bottom = 8.dp),
         horizontalArrangement = Arrangement.spacedBy(44.dp)
      ) {
         iOSStyleIconButton(
            {},
            Modifier.size(48.dp, 48.dp).autorepeatingClickable(
               remember { MutableInteractionSource() },
               fireAction = { viewModel.backspace() }
            ),
            placement = IconButtonPlacement.Content
         ) {
            BlueIcon(
               painterProvider.provide(PainterProvider.Resource.Backspace),
               "delete last letter",
               Modifier.requiredWidth(40.dp).requiredHeight(32.dp)
            )
         }
         iOSStyleIconButton(
            {
               coroutineScope.launch {
                  viewModel.enter()
               }
            },
            Modifier.size(48.dp, 48.dp),
            enabled = viewModel.enterEnabled,
            placement = IconButtonPlacement.Content
         ) {
            BlueIcon(
               painterProvider.provide(PainterProvider.Resource.Enter),
               "submit the entered letters",
               Modifier.requiredWidth(46.dp).requiredHeight(32.dp)
            )
         }
      }
   }
}

@Composable fun EnteredWordsSheet(words: List<IEnteredWord>) {
   log.d { "Entered words: ${words.map { it.value }}" }
   val modifier = Modifier.fillMaxWidth()
      .padding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues())
      .padding(horizontal = 16.dp, vertical = 12.dp)
   Column(modifier) {
      Text("Entered words", style = headlineStyle, maxLines = 1)
      VerticalGrid(
         SimpleGridCells.Adaptive(100.dp),
         Modifier.padding(top = 12.dp)
      ) {
         val sortedWords = words.sortedBy { it.value }
            .map { it.value.replaceFirstChar(Char::titlecaseChar) }
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
