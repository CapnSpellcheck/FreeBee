package com.letstwinkle.freebee.screens.loader

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.compose.MyAppTheme
import com.letstwinkle.freebee.screens.BackNavigator
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GameLoaderScreen(
   gameDate: LocalDate,
   backNavigator: BackNavigator,
   loaderNavigator: GameLoaderNavigator,
) {
   MyAppTheme {
      Scaffold(topBar = {
         CenterAlignedTopAppBar(
            title = { Text(formatGameDateToDisplay(gameDate)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = backNavigationButton(backNavigator::goBack),
         )
      }) {
         GameLoader(gameDate, navigator = loaderNavigator, modifier = Modifier.padding(it))
      }
   }
}

@Composable fun GameLoader(
   gameDate: LocalDate,
   viewModel: GameLoaderViewModel = viewModel { GameLoaderViewModel(gameDate, repository()) },
   navigator: GameLoaderNavigator?,
   modifier: Modifier = Modifier,
) {
   LaunchedEffect(Unit) {
      viewModel.load()
   }
   LaunchedEffect(viewModel.status.value) {
      (viewModel.status.value as? LoadingStatus.Finished)?.let { finished ->
         navigator?.openGame(gameDate, finished.gameID)
      }
   }
   Column(modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) { 
      Text(viewModel.status.value.statusText, Modifier.padding(bottom = 8.dp), style = bodyStyle)
      if (viewModel.status.value.showProgress) {
         CircularProgressIndicator(
            modifier = Modifier.width(38.dp),
            color = MaterialTheme.colors.secondary
         )
      }
   }
}
