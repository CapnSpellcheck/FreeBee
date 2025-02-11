package com.letstwinkle.freebee.screens.root

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letstwinkle.freebee.database.FreeBeeRepository
import com.letstwinkle.freebee.database.Game
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

private val log = logging()

class GameListViewModel(private val repository: FreeBeeRepository) : ViewModel() {
   private val gamesFlowMutable: MutableStateFlow<List<Game>> = MutableStateFlow(emptyList())
   val gamesFlow: StateFlow<List<Game>> = gamesFlowMutable.asStateFlow()
   
   init {
      log.d { "init" }
      viewModelScope.launch {
         log.d { "launch fetchGamesLive" }
         repository.fetchGamesLive().collect { games ->
            log.d { "fetchGamesLive yielded" }
            gamesFlowMutable.value = games
         }
      }
   }
   
   override fun onCleared() {
      super.onCleared()
      viewModelScope.cancel()
   }
   
   fun openPayPal(uriHandler: UriHandler, clipManager: ClipboardManager) {
      clipManager.setText(AnnotatedString("jpellico@gmail.com"))
      uriHandler.openUri("https://www.paypal.com/myaccount/transfer/homepage")
   }
}
