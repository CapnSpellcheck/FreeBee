package com.letstwinkle.freebee

import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format

private val gameURLBuilder = URLBuilder(host = "nytbee.com", protocol = URLProtocol.HTTPS)

fun gameURL(gameDate: LocalDate): Url {
   gameURLBuilder.set(path = "Bee_${gameDate.format(LocalDate.Formats.ISO_BASIC)}.html")
   return gameURLBuilder.build()
}

fun gameImageURL(gameDate: LocalDate): Url {
   gameURLBuilder.set(path = "/pics/${gameDate.format(LocalDate.Formats.ISO_BASIC)}.png")
   return gameURLBuilder.build()
}
