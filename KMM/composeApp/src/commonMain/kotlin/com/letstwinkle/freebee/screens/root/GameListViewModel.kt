package com.letstwinkle.freebee.screens.root

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letstwinkle.freebee.SettingKeys
import com.letstwinkle.freebee.database.FreeBeeRepository
import com.letstwinkle.freebee.database.IGame
import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.lighthousegames.logging.logging

private val log = logging()

class GameListViewModel<Id, Game: IGame<Id>>(
   private val repository: FreeBeeRepository<Id, Game, *>,
   multiplatformSettings: Settings = Settings(),
) : ViewModel()
{
   private val orderByScoredState = mutableStateOf(false)
   private var orderByScoredSetting: Boolean by multiplatformSettings.boolean(SettingKeys.OrderGameListByScoredAt, true)
   private val gamesFlowMutable: MutableStateFlow<List<Game>> = MutableStateFlow(emptyList())
   private var fetchJob: Job? = null
   
   val gamesFlow: StateFlow<List<Game>> = gamesFlowMutable.asStateFlow()
   
   var orderByScored: Boolean
      get() = orderByScoredState.value
      set(value) {
         if (value != orderByScoredState.value) {
            orderByScoredSetting = value
            orderByScoredState.value = value
            refresh()
         }
      }
   
   init {
      log.d { "init" }
      orderByScoredState.value = orderByScoredSetting
      refresh()
   }
   
   private fun refresh() {
      fetchJob?.cancel()
      fetchJob = viewModelScope.launch {
         log.d { "launch fetchGamesLive" }
         repository.fetchGamesLive(orderByScored).collect { games ->
            println("yielding games: $games  orderBy=$orderByScored")
            log.d { "fetchGamesLive yielded" }
            gamesFlowMutable.value = games
         }
      }
   }
   
   fun openPayPal(uriHandler: UriHandler, clipManager: ClipboardManager) {
      clipManager.setText(AnnotatedString("jpellico@gmail.com"))
      uriHandler.openUri("https://www.paypal.com/myaccount/transfer/homepage")
   }
   
   override fun onCleared() {
      super.onCleared()
      viewModelScope.cancel()
   }
}
