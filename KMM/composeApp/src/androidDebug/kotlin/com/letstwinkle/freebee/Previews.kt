package com.letstwinkle.freebee

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letstwinkle.freebee.database.Game
import com.letstwinkle.freebee.database.IGame
import com.letstwinkle.freebee.model.StatisticsModel
import com.letstwinkle.freebee.screens.BackNavigator
import com.letstwinkle.freebee.screens.Statistics
import com.letstwinkle.freebee.screens.game.*
import com.letstwinkle.freebee.screens.loader.GameLoader
import com.letstwinkle.freebee.screens.picker.GamePicker
import com.letstwinkle.freebee.screens.root.GameList
import com.letstwinkle.freebee.screens.root.GameListNavigator
import kotlinx.coroutines.launch
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
@Preview(
   apiLevel = 34,
   device = "id:pixel_8"
)
fun GameListPreview() {
   Scaffold(topBar = {
      TopAppBar(
         title = { Text("Games") },
      )}) {
      GameList(
         PreviewRepository(),
         painterProvider = PreviewPainterProvider(),
         navigator = object : GameListNavigator<Game> {
            override fun showStatistics() {
            }
            override fun openGame(game: Game) {
            }
            override fun openGamePicker() {
            }
         })
   }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
@Preview(
   apiLevel = 34,
   device = "id:pixel_8"
)
fun StatisticsPreview() {
   val statisticsModel = rememberSaveable { mutableStateOf<StatisticsModel?>(null) }
   LaunchedEffect(Unit) {
      statisticsModel.value = StatisticsModel(
         PreviewRepository(),
         PreviewSettings().apply {
            putInt(SettingKeys.PangramCount, 50)
         }
      )
   }
   Scaffold(topBar = {
      TopAppBar(
         title = { Text("Statistics") },
      )
   }) {
      statisticsModel.value?.let { Statistics(it) }
   }
}

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
@Preview(
   apiLevel = 34,
   device = "id:pixel_6"
)
fun GamePreview() {
   val gameViewModel = GameViewModel(PreviewRepository(), 3L, PreviewSettings())
   val rulesState =
      rememberModalBottomSheetState(ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
   val coroutineScope = rememberCoroutineScope()
   
   Scaffold(topBar = {
      TopAppBar(
         title = { Text("Apr 7, 2020")},
         actions = {
            IconButton( { coroutineScope.launch { rulesState.show() } }) {
               Icon(PreviewPainterProvider().provide(PainterProvider.Resource.Rules), null)
            }
         })
   }) {
      GameWithSheets(gameViewModel, rulesState, painterProvider = PreviewPainterProvider())
   }
}

@Composable
@Preview(
   apiLevel = 34,
   device = "id:pixel_8"
)
fun RulesPreview() {
   RulesSheet()
}

@Composable
@Preview
fun HoneycombPreview() {
   LetterHoneycomb(centerLetter = 'e', otherLetters = "yatpcf".toList(), onLetterTap = {}, modifier = Modifier.padding(16.dp))
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
@Preview(
   apiLevel = 34,
   device = "id:pixel_6"
)
fun GamePickerPreview() {
   GamePicker(PreviewRepository())
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
@Preview(
   apiLevel = 34,
   device = "id:pixel_6"
)
fun GameLoaderPreview() {
   val gameDate = LocalDate.now().toKotlinLocalDate()
   GameLoader(PreviewRepository(), gameDate, navigator = null, backNavigator = object : BackNavigator {
      override fun goBack() {}
   })
}
