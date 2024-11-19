package com.letstwinkle.freebee.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.database.IGame
import com.letstwinkle.freebee.database.IGameWithWords
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

private val log = logging()

@Composable fun GameScreen(
   game: IGame,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   MaterialTheme {
      val gameViewModel = GameViewModel(repository(), game.uniqueID)
      val dateString = formatGameDateToDisplay(game.date)
      
      Scaffold(topBar = {
         TopAppBar(
            title = { Text(dateString)},
            actions = {
            })
      }) {
         Game(gameViewModel, painterProvider = painterProvider)
      }
   }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun <GameWithWords: IGameWithWords> Game(
   viewModel: GameViewModel<GameWithWords>,
   modifier: Modifier = Modifier,
   painterProvider: PainterProvider = ResourcePainterProvider(),
) {
   val gameWithWords = viewModel.gameWithWords.value
   val coroutineScope = rememberCoroutineScope()
   
   Column(modifier.fillMaxSize()) {
      Text("Score: ${gameWithWords?.game?.score ?: ""}")
      FlowRow(modifier.height(100.dp).fillMaxWidth(), maxItemsInEachRow = 3) { 
         gameWithWords?.enteredWords?.forEach { enteredWord -> 
            Text(enteredWord.value)
         }
      }
      
      // Temporary input mechanism for a word instead of the letter honeycomb
      BasicTextField(gameWithWords?.game?.currentWord ?: "", singleLine = true, onValueChange = {
         log.d { "BasicTextField onValueChanged" }
         viewModel.updateCurrentWord(it)
      })
      
      TextButton({
         coroutineScope.launch {
            viewModel.enter()
         }
      }) {
         Text("Enter")
      }
   }
}
