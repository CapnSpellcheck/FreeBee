@file:Suppress("MemberVisibilityCanBePrivate")

package com.letstwinkle.freebee.util

import com.letstwinkle.freebee.screens.root.GameListNavigator

class MockGameListNavigator : GameListNavigator<MockGame> {
   var showStatisticsCount = 0; private set
   var openGamePickerCount = 0; private set
   var openGameCount = 0; private set
   var lastGameOpened: MockGame? = null; private set
   
   val onShowStatistics: (() -> Unit)?
   val onOpenGamePicker: (() -> Unit)?
   val onOpenGame: ((MockGame) -> Unit)?

   constructor(
      onShowStatistics: (() -> Unit)? = null,
      onOpenGamePicker: (() -> Unit)? = null,
      onOpenGame: ((MockGame) -> Unit)? = null,
   ) {
      this.onShowStatistics = onShowStatistics
      this.onOpenGamePicker = onOpenGamePicker
      this.onOpenGame = onOpenGame
   }
   
   override fun showStatistics() {
      onShowStatistics?.invoke()
      showStatisticsCount++
   }
   
   override fun openGamePicker() {
      onShowStatistics?.invoke()
      openGamePickerCount++
   }
   
   override fun openGame(game: MockGame) {
      lastGameOpened = game
      onOpenGame?.invoke(game)
      openGameCount++
   }
   
}
