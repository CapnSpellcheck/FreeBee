package com.letstwinkle.freebee

import androidx.compose.runtime.Composable
import com.letstwinkle.freebee.database.*
import kotlinx.datetime.LocalDate

interface Platform {
   val name: String
}

expect fun getPlatform(): Platform

expect fun formatGameDateToDisplay(date: LocalDate): String

expect fun backNavigationButton(onClick: () -> Unit): @Composable () -> Unit
