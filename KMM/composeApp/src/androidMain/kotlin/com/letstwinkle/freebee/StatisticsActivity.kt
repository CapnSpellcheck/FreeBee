package com.letstwinkle.freebee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.letstwinkle.freebee.screens.StatisticsScreen

class StatisticsActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      setContent {
         StatisticsScreen(this.backNavigator())
      }
   }
}
