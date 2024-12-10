package com.letstwinkle.freebee.screens.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letstwinkle.freebee.database.*
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

private val log = logging()

class GameListViewModel(private val repository: FreeBeeRepository) : ViewModel() {
   private val gamesFlowMutable: MutableStateFlow<List<Game>> = MutableStateFlow(emptyList())
   val gamesFlow: CStateFlow<List<Game>> = gamesFlowMutable.asStateFlow().cStateFlow()
   
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
}
