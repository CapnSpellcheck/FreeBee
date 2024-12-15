package com.letstwinkle.freebee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.letstwinkle.freebee.screens.picker.GamePickerNavigator
import com.letstwinkle.freebee.screens.picker.GamePickerScreen
import kotlinx.datetime.LocalDate

class GamePickerActivity : ComponentActivity(), GamePickerNavigator {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      setContent {
         GamePickerScreen(this.backNavigator(), this)
      }
   }
   
   override fun openGameLoader(gameDate: LocalDate) {
   }
}
