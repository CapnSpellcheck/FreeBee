package com.letstwinkle.freebee.screens.root

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.compose.*
import com.letstwinkle.freebee.database.*

private val TableHorizontalPadding = 16.dp

enum class GameListHeader(val label: String) {
   InProgress("In progress"),
   Completed("100% complete"),
   New("Start a new game"),
   ;
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun <Id, Game: IGame<Id>> GameListScreen(
   repository: FreeBeeRepository<Id, Game, *>,
   navigator: GameListNavigator<Game>?,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   val viewModel = viewModel { GameListViewModel(repository) }
   
   MyAppTheme {
      
      Scaffold(topBar = {
         CenterAlignedTopAppBar(
            { Text("Games") },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            actions = {
               var expanded by rememberSaveable { mutableStateOf(false) }
               iOSStyleIconButton( { navigator?.showStatistics() }) {
                  val painter = painterProvider.provide(PainterProvider.Resource.ChartBarXaxis)
                  AccentIcon(contentDescription = "Statistics", painter = painter)
               }
               iOSStyleIconButton( { expanded = true }) {
                  val painter = painterProvider.provide(PainterProvider.Resource.Sort)
                  AccentIcon(contentDescription = "Sort by", painter = painter)
                  DropdownMenu(expanded, { expanded = false }) {
                     val checkmark = @Composable {
                        Icon(
                           imageVector = Icons.Default.Check,
                           contentDescription = "this sort method is selected"
                        )
                     }
                     val placeholder = @Composable { Box(Modifier) }
                     DropdownMenuItem(
                        { Text("Recently played") },
                        { viewModel.orderByScored = true },
                        trailingIcon = if (viewModel.orderByScored) checkmark else placeholder
                     )
                     DropdownMenuItem(
                        { Text("Game date") },
                        { viewModel.orderByScored = false },
                        trailingIcon = if (viewModel.orderByScored) placeholder else checkmark
                     )
                  }
               }
            })
      }) {
         GameList(viewModel, navigator, Modifier.padding(it), painterProvider)
      }
   }
}

@Composable fun <Id, Game: IGame<Id>> GameList(
   viewModel: GameListViewModel<Id, Game>,
   navigator: GameListNavigator<Game>?,
   modifier: Modifier = Modifier,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   val gamesGroupedByComplete = viewModel.gamesFlow.collectAsState().value
      .groupBy { it.isComplete }.withDefault { emptyList() }
   val chevron = painterProvider.provide(PainterProvider.Resource.Chevron)
   val onGameClick = fun(game: Game) {
      navigator?.openGame(game)
   }
   Column(modifier.background(groupedTableHeaderBackgroundColor)) {
      LazyColumn(
         Modifier.weight(1f).padding(bottom = 16.dp)
      ) {
         item(key = "inprogress") {
            Header(GameListHeader.InProgress)
         }
         GameGroup(gamesGroupedByComplete.getValue(false), onGameClick, chevron)
         if (gamesGroupedByComplete.containsKey(true)) {
            item(key = "completed") {
               Header(GameListHeader.Completed)
            }
            GameGroup(gamesGroupedByComplete.getValue(true), onGameClick, chevron)
         }
         item(key = "newheader") {
            Header(GameListHeader.New)
         }
         item(key = "newrow") {
            val rowModifier = rowBaseModifier.clickable(onClickLabel = "Open a new game") {
               navigator?.openGamePicker()
            }
            Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
               Text("Choose a new game", style = bodyStyle)
            }
         }
      }
      val uriHandler = LocalUriHandler.current
      val clipManager = LocalClipboardManager.current
      SponsorMe() { viewModel.openPayPal(uriHandler, clipManager) }
   }
}

@Composable private fun Header(header: GameListHeader) {
   Text(
      header.label,
      Modifier.heightIn(28.dp)
         .padding(TableHorizontalPadding, 17.dp, TableHorizontalPadding, 6.dp),
      color = groupedTableHeaderTextColor,
      fontSize = 13.sp,
      letterSpacing = 0.5.sp
   )
}

@OptIn(ExperimentalFoundationApi::class)
private fun <Game : IGame<*>> LazyListScope.GameGroup(games: List<Game>, onClick: (Game) -> Unit, chevron: Painter) {
   if (games.isNotEmpty()) {
      item {
         fullBleedDivider()
      }
   }
   itemsIndexed(games, { _, game -> game.uniqueID as Any }) { index, game ->
      GameRow(game, onClick, Modifier.animateItemPlacement(), chevron)
      if (index < games.size - 1) {
         indentedDivider(TableHorizontalPadding)
      }
   }
   if (games.isNotEmpty()) {
      item {
         fullBleedDivider()
      }
   }
}

@Composable private fun <Game: IGame<*>> GameRow(
   game: Game,
   onClick: (Game) -> Unit,
   modifier: Modifier = Modifier,
   chevron: Painter,
) {
   Row(
      modifier.then(rowBaseModifier)
         .clickable(onClickLabel = "Open this game") { onClick(game) },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
   ) {
      Text(formatGameDateToDisplay(game.date), Modifier.weight(1f), style = bodyStyle)
      CompositionLocalProvider(
         LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.5f)
         )
      ) {
         val annotatedString = buildAnnotatedString {
            pushStyle(
               SpanStyle(
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 2.sp
               )
            )
            append(game.otherLetters.substring(0..<3))
            pushStyle(SpanStyle(color = yellowAccentColor))
            append(Char(game.centerLetterCode))
            pop()
            append(game.otherLetters.substring(3))
         }
         Text(
            annotatedString.toUpperCase(LocaleList(Locale("en"))),
            Modifier.padding(horizontal = 8.dp),
            fontFamily = gameLettersFontFamily(),
         )
      }
      Row(
         Modifier.weight(1f),
         horizontalArrangement = Arrangement.End,
         verticalAlignment = Alignment.CenterVertically
      ) {
         Text(if (game.isComplete) "💯" else "Score: ${game.score}", style = bodyStyle)
         Image(
            chevron,
            null,
            modifier = Modifier.padding(start = 10.dp)
               .width(with(LocalDensity.current) { 10.sp.toDp() }),
            colorFilter = ColorFilter.tint(disclosureIndicatorColor)
         )
      }
   }
}

private val rowBaseModifier = Modifier.fillMaxWidth()
   .heightIn(44.dp)
   .background(Color.White)
   .padding(TableHorizontalPadding, 0.dp)

@Composable private fun SponsorMe(openPayPal: () -> Unit) {
   val showPaypalAlert = remember { mutableStateOf(false) }
   Column(
      Modifier.clip(RoundedCornerShape(6.dp)).background(Color.White).padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
   ) {
      Text("Sponsor me", style = headlineStyle)
      Text("I appreciate tips! The only social payment platform I'm on is PayPal. Feel free to send me a gift.", style = com.letstwinkle.freebee.footnoteStyle)
      TextButton(
         { showPaypalAlert.value = true },
         Modifier.align(Alignment.CenterHorizontally),
         colors = ButtonDefaults.textButtonColors(contentColor = iOSInspiredBlueActionColor),
      ) {
         Text("Open PayPal")
      }
   }
   val dismiss = { showPaypalAlert.value = false }
   if (showPaypalAlert.value) {
      AlertDialog(
         dismiss,
         confirmButton = { Button({ openPayPal(); dismiss() }) { Text("Continue")} },
         text = { Text("Taking you to the PayPal send money page. It may open in a browser. My email address has been copied to the clipboard, please paste it into the recipient field.") },
      )
   }
}

@Composable private fun fullBleedDivider() {
   Divider(Modifier.testTag("divider"), color = rowDividerColor)
}
