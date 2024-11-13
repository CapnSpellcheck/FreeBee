package com.letstwinkle.freebee.screens.root

import com.letstwinkle.freebee.database.IGame

interface GameListNavigator {
   fun showStatistics()
   fun openGame(game: IGame)
   fun openGamePicker()
}