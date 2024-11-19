package com.letstwinkle.freebee

import com.letstwinkle.freebee.database.*
import kotlinx.datetime.Instant

interface Platform {
   val name: String
}

expect fun getPlatform(): Platform

expect fun formatGameDateToDisplay(date: Instant): String

expect fun repository(): FreeBeeRepository<out IGame, out IGameWithWords>
