package com.letstwinkle.freebee.screens.root

import com.letstwinkle.freebee.database.IGame

interface GameListNavigator<Game: IGame<*>> {
   fun showStatistics()
   fun openGame(game: Game)
   fun openGamePicker()
}
