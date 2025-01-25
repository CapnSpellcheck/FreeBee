package com.letstwinkle.freebee.screens.root

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.compose.*
import com.letstwinkle.freebee.database.Game
import com.letstwinkle.freebee.database.isComplete
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val TableHorizontalPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GameListScreen(
   navigator: GameListNavigator?,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   MyAppTheme {
      val painter = painterProvider.provide(PainterProvider.Resource.ChartBarXaxis)
      
      Scaffold(topBar = {
         CenterAlignedTopAppBar(
            { Text("Games") },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            actions = {
               iOSStyleIconButton( { navigator?.showStatistics() }) {
                  AccentIcon(contentDescription = "Statistics", painter = painter)
               }
            })
      }) {
         GameList(
            navigator = navigator,
            modifier = Modifier.padding(it),
            painterProvider = painterProvider
         )
      }
   }
}

@OptIn(ExperimentalResourceApi::class)
@Composable fun GameList(
   viewModel: GameListViewModel = viewModel { GameListViewModel(repository()) },
   navigator: GameListNavigator?,
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
            Header("In progress")
         }
         GameGroup(gamesGroupedByComplete.getValue(false), onGameClick, chevron)
         if (gamesGroupedByComplete.containsKey(true)) {
            item(key = "completed") {
               Header("100% complete")
            }
            GameGroup(gamesGroupedByComplete.getValue(true), onGameClick, chevron)
         }
         item(key = "newheader") {
            Header("Start a new game")
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

@Composable private fun Header(label: String) {
   Text(
      label.uppercase(),
      Modifier.heightIn(28.dp).padding(TableHorizontalPadding, 17.dp, TableHorizontalPadding, 6.dp),
      color = groupedTableHeaderTextColor,
      fontSize = 13.sp,
      letterSpacing = 0.5.sp
   )
}

private fun LazyListScope.GameGroup(games: List<Game>, onClick: (Game) -> Unit, chevron: Painter) {
   if (games.isNotEmpty()) {
      item {
         fullBleedDivider()
      }
   }
   itemsIndexed(games, { _, game -> game.uniqueID }) { index, game ->
      GameRow(game, onClick, chevron = chevron)
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

@Composable private fun GameRow(
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

private val rowBaseModifier = Modifier.fillMaxWidth()
   .heightIn(44.dp)
   .background(Color.White)
   .padding(TableHorizontalPadding, 0.dp)

@Composable fun fullBleedDivider() {
   Divider(color = rowDividerColor)
}
