package com.letstwinkle.freebee

import android.app.Application

class FreeBeeApplication : Application() {
   override fun onCreate() {
      super.onCreate()
      setApplicationContext(this)
   }
}