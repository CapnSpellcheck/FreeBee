package com.letstwinkle.freebee.screens.root

import com.letstwinkle.freebee.database.FreeBeeRepository
import com.letstwinkle.freebee.database.IGame
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameListViewModel(private val repository: FreeBeeRepository) : ViewModel() {
   private val gamesFlowMutable: MutableStateFlow<List<IGame>> = MutableStateFlow(emptyList())
   val gamesFlow: CStateFlow<List<IGame>> = gamesFlowMutable.asStateFlow().cStateFlow()
   
   init {
      viewModelScope.launch { 
         repository.fetchGamesLive().collect { games ->
            gamesFlowMutable.value = games
         }
      }
   }
   
   override fun onCleared() {
      super.onCleared()
      viewModelScope.cancel()
   }
}