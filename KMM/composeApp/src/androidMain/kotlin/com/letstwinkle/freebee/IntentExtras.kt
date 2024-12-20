@file:Suppress("NOTHING_TO_INLINE")

package com.letstwinkle.freebee

import android.content.Intent
import androidx.core.content.IntentCompat
import com.letstwinkle.freebee.database.EntityIdentifier
import com.letstwinkle.freebee.database.Game
import kotlinx.datetime.LocalDate


inline fun Intent.putGameExtra(game: Game) {
   putExtra("game", game)
}

inline fun Intent.getGameExtra(): Game? = IntentCompat.getParcelableExtra(this, "game", Game::class.java)

inline fun Intent.hasGame(): Boolean = hasExtra("game")

inline fun Intent.putGameDateExtra(date: LocalDate) {
   putExtra("gamedate", date.toEpochDays())
}

fun Intent.getGameDateExtra(): LocalDate = LocalDate.fromEpochDays(getIntExtra("gamedate", 0))

inline fun Intent.hasGameDate(): Boolean = hasExtra("gamedate")

inline fun Intent.putGameIdentifierExtra(gameID: EntityIdentifier) {
   putExtra("gameid", gameID)
}

inline fun Intent.getGameIdentifierExtra(): EntityIdentifier = getLongExtra("gameid", -1)

inline fun Intent.hasGameIdentifier(): Boolean = hasExtra("gameid")
