package com.letstwinkle.freebee.screens.loader

import kotlinx.datetime.LocalDate

interface GameLoaderNavigator<Identifier> {
   fun openGame(gameDate: LocalDate, gameID: Identifier)
}
