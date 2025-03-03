package com.letstwinkle.freebee.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.letstwinkle.freebee.backNavigationButton
import com.letstwinkle.freebee.bodyStyle
import com.letstwinkle.freebee.compose.MyAppTheme
import com.letstwinkle.freebee.compose.indentedDivider
import com.letstwinkle.freebee.database.AnyFreeBeeRepository
import com.letstwinkle.freebee.model.StatisticsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun StatisticsScreen(repository: AnyFreeBeeRepository, backNavigator: BackNavigator) {
   MyAppTheme {
      val statisticsModel = rememberSaveable { mutableStateOf<StatisticsModel?>(null) }
      LaunchedEffect(Unit) {
         statisticsModel.value = StatisticsModel(repository)
      }
      Scaffold(topBar = {
         CenterAlignedTopAppBar(
            { Text("Statistics") },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = backNavigationButton(backNavigator::goBack),
         )
      }) {
         statisticsModel.value?.let { Statistics(it) }
      }
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
