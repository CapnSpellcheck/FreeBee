package com.letstwinkle.freebee

import android.content.Context
import com.letstwinkle.freebee.database.FreeBeeRepository
import com.letstwinkle.freebee.database.RoomDatabase

private lateinit var applicationContext: Context

fun setApplicationContext(context: Context) {
   applicationContext = context
}

actual fun repository(): FreeBeeRepository {
   return RoomDatabase.getDatabase(applicationContext)
}