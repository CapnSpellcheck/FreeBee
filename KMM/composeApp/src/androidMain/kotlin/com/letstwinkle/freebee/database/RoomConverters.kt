package com.letstwinkle.freebee.database

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class RoomConverters {
   @TypeConverter fun convertAllowedWordsFromModelToPersistence(allowed: Set<String>): String =
      allowed.joinToString(separator = "|")
   @TypeConverter fun convertAllowedWordsFromPersistenceToModel(stored: String): Set<String> =
      stored.split('|').toHashSet()
   
   @TypeConverter fun convertInstantFromModelToPersistence(instant: Instant): Long = 
      instant.epochSeconds
   @TypeConverter fun convertInstantFromPersistenceToModel(long: Long): Instant =
      Instant.fromEpochSeconds(long)
}