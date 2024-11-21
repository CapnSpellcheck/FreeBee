package com.letstwinkle.freebee.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letstwinkle.freebee.bodyStyle
import com.letstwinkle.freebee.indentedDivider
import com.letstwinkle.freebee.statistics.StatisticsModel

@Composable fun StatisticsScreen() {
   val statisticsModel = rememberSaveable { mutableStateOf<StatisticsModel?>(null) }
   LaunchedEffect(Unit) {
      statisticsModel.value = StatisticsModel()
   }
   Scaffold(topBar = {
      TopAppBar(
         title = { Text("Statistics") },
      )
   }) {
      statisticsModel.value?.let { Statistics(it) }
   }
}

private val padding = 16.dp

@Composable fun Statistics(model: StatisticsModel) {
   Column {
      StatisticRow("Games started", model.gamesStarted)
      indentedDivider(padding)
      StatisticRow("Words played", model.wordsPlayed)
      indentedDivider(padding)
      StatisticRow("Pangrams", model.pangramsPlayed)
      indentedDivider(padding)
      StatisticRow("Genius reached", model.geniusGames)
      indentedDivider(padding)
   }
}

@Composable fun StatisticRow(label: String, value: Int) {
   Row(
      Modifier.fillMaxWidth().heightIn(44.dp).padding(padding, 11.dp, padding, 11.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
   ) {
      Text(label, style = bodyStyle)
      Text(value.toString(), fontSize = 24.sp)
   }
}
