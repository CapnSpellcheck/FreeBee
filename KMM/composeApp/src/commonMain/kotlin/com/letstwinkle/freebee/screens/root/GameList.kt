package com.letstwinkle.freebee.screens.root

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
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

@Composable fun GameListScreen(
   navigator: GameListNavigator?,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   MyAppTheme {
      val painter = painterProvider.provide(PainterProvider.Resource.ChartBarXaxis)
      
      Scaffold(topBar = {
         TopAppBar(
            { Text("Games") },
            AppBarDefaults.topAppBarWindowInsets,
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
   val games = viewModel.gamesFlow.collectAsState().value
   Column(modifier.background(groupedTableHeaderBackgroundColor)) {
      LazyColumn(
         Modifier.weight(1f)
      ) {
         item(key = "inprogress") {
            Header("In progress")
         }
         if (games.isNotEmpty()) {
            item {
               fullBleedDivider()
            }
         }
         itemsIndexed(games, { _, game -> game.uniqueID }) { index, game ->
            GameRow(game, { navigator?.openGame(game) }, painterProvider = painterProvider)
            if (index < games.size - 1) {
               indentedDivider(TableHorizontalPadding)
            }
         }
         if (games.isNotEmpty()) {
            item {
               fullBleedDivider()
            }
         }
         item(key = "new") {
            Header("Start a new game")
         }
      }
      Column(Modifier.clip(RoundedCornerShape(6.dp)).background(Color.White), verticalArrangement = Arrangement.spacedBy(12.dp)) {
         Text("Sponsor me")
         Text("I appreciate tips! The only social payment platform I'm on is PayPal. Feel free to send me a gift.")
         TextButton({ }) {
            Text("Open PayPal")
         }
      }
   }
}

@Composable fun Header(label: String) {
   Text(
      label.uppercase(),
      Modifier.heightIn(28.dp).padding(TableHorizontalPadding, 17.dp, TableHorizontalPadding, 6.dp),
      color = groupedTableHeaderTextColor,
      fontSize = 13.sp,
      letterSpacing = 0.5.sp
   )
}

@Composable fun GameRow(
   game: Game,
   onClick: (Game) -> Unit,
   modifier: Modifier = Modifier,
   painterProvider: PainterProvider,
) {
   Row(
      modifier.fillMaxWidth()
         .heightIn(44.dp)
         .background(Color.White)
         .clickable(onClickLabel = "Open this game") { onClick(game) }
         .padding(TableHorizontalPadding, 0.dp),
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
            painterProvider.provide(PainterProvider.Resource.Chevron),
            null,
            modifier = Modifier
               .padding(start = 10.dp)
               .width(with(LocalDensity.current) { 10.sp.toDp() }),
            colorFilter = ColorFilter.tint(disclosureIndicatorColor)
         )
      }
   }
}

@Composable fun fullBleedDivider() {
   Divider(color = rowDividerColor)
}
