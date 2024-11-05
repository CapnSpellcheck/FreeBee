package com.letstwinkle.freebee

import android.app.Application
import com.letstwinkle.freebee.database.RoomDatabase

class FreeBeeApplication : Application() {
   override fun onCreate() {
      super.onCreate()
      setApplicationContext(this)
   }
}