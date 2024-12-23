package com.letstwinkle.freebee.screens.loader

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.compose.MyAppTheme
import com.letstwinkle.freebee.screens.BackNavigator
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
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
         GameLoader(
            gameDate,
            navigator = loaderNavigator,
            backNavigator = backNavigator,
            modifier = Modifier.padding(it)
         )
      }
   }
}

@Composable fun GameLoader(
   gameDate: LocalDate,
   viewModel: GameLoaderViewModel = viewModel { GameLoaderViewModel(gameDate, repository()) },
   navigator: GameLoaderNavigator?,
   backNavigator: BackNavigator,
   modifier: Modifier = Modifier,
) {
   val status = viewModel.status.value
   LaunchedEffect(Unit) {
      viewModel.load(onCenterLetterNotUnique = { candidates ->
         centerLetterCandidates.value = candidates
         val result = centerLetterResultChannel.receive()
         centerLetterCandidates.value = emptyList()
         result
      })
   }

   LaunchedEffect(status) {
      (status as? LoadingStatus.Finished)?.let { finished ->
         navigator?.openGame(gameDate, finished.gameID)
      }
   }
   
   Column(modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) { 
      Text(status.statusText, Modifier.padding(bottom = 8.dp), style = bodyStyle)
      if (status.showProgress) {
         CircularProgressIndicator(
            modifier = Modifier.width(38.dp),
            color = MaterialTheme.colors.secondary
         )
      }
   }
   if (status is LoadingStatus.Error) {
      val error = status.error.message ?: "Unable to load the game"
      AlertDialog(
         {},
         { Button({ backNavigator.goBack() }) { Text("OK") } },
         title = { Text("Error") },
         text = { Text(error) },
         properties = DialogProperties(dismissOnClickOutside = false)
      )
   }
}
