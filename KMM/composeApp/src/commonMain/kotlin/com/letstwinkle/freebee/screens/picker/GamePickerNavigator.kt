package com.letstwinkle.freebee.screens.picker

import kotlinx.datetime.LocalDate

interface GamePickerNavigator {
   fun openGameLoader(gameDate: LocalDate)
}
