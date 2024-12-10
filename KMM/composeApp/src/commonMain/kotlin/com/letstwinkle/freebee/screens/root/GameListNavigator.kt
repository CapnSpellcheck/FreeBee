package com.letstwinkle.freebee.screens.root

import com.letstwinkle.freebee.database.Game

interface GameListNavigator {
   fun showStatistics()
   fun openGame(game: Game)
   fun openGamePicker()
}
