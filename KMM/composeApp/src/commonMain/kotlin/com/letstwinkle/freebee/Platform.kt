package com.letstwinkle.freebee

interface Platform {
   val name: String
}

expect fun getPlatform(): Platform