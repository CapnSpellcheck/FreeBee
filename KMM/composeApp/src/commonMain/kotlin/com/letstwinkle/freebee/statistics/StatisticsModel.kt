package com.letstwinkle.freebee.statistics

import com.letstwinkle.freebee.SettingKeys
import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.repository
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get

data class StatisticsModel(
   val gamesStarted: Int,
   val wordsPlayed: Int,
   val pangramsPlayed: Int,
   val geniusGames: Int,
)

suspend fun StatisticsModel(
   database: CovariantFreeBeeRepository = repository(),
   settings: Settings = Settings()
): StatisticsModel = 
   StatisticsModel(
      database.getStartedGameCount(),
      database.getEnteredWordCount(),
      settings[SettingKeys.PangramCount, 0],
      database.getGeniusGameCount()
   )
