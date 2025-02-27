package com.letstwinkle.freebee.database.android

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class MigrationChangingEnteredWordIndexes : Migration(6, 7) {
   override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("DROP INDEX IF EXISTS `index_EnteredWord_gameId`")
      db.execSQL("CREATE INDEX `index_EnteredWord_value` ON `EnteredWord` (`value`)")
   }
}
