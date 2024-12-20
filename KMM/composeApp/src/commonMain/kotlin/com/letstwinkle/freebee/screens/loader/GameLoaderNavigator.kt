package com.letstwinkle.freebee.screens.loader

import com.letstwinkle.freebee.database.EntityIdentifier
import kotlinx.datetime.LocalDate

interface GameLoaderNavigator {
   fun openGame(gameDate: LocalDate, gameID: EntityIdentifier)
}
