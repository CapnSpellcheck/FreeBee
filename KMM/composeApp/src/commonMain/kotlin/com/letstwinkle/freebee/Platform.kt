package com.letstwinkle.freebee

import kotlinx.datetime.Instant

interface Platform {
   val name: String
}

expect fun getPlatform(): Platform

expect fun formatGameDateToDisplay(date: Instant): String