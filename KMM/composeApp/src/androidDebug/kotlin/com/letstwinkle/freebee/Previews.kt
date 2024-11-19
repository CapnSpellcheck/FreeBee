package com.letstwinkle.freebee

import android.annotation.SuppressLint
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import com.letstwinkle.freebee.database.IGame
import com.letstwinkle.freebee.screens.root.*
import com.letstwinkle.freebee.screens.Statistics
import com.letstwinkle.freebee.screens.game.Game
import com.letstwinkle.freebee.screens.game.GameViewModel
import com.letstwinkle.freebee.statistics.StatisticsModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
@Preview(
   apiLevel = 34,
   device = "id:pixel_8"
)
fun GameListPreview() {
   val gameListViewModel = GameListViewModel(PreviewRepository())
   Scaffold(topBar = {
      TopAppBar(
         title = { Text("Games") },
      )}) {
      GameList(
         gameListViewModel,
         painterProvider = PreviewPainterProvider(),
         navigator = object : GameListNavigator {
            override fun showStatistics() {
            }
            
            override fun openGame(game: IGame) {
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

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
@Preview(
   apiLevel = 34,
   device = "id:pixel_8"
)
fun GamePreview() {
   val gameViewModel = GameViewModel(PreviewRepository(), 3)
   Scaffold(topBar = {
      TopAppBar(
         title = { Text("Mar 15, 2023") },
         actions = {
         })
   }) {
      Game(gameViewModel, painterProvider = PreviewPainterProvider())
   }
}

