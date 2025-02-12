package com.letstwinkle.freebee

import android.content.Intent
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
         GamePickerScreen(repository(), this.backNavigator(), this)
      }
   }
   
   override fun openGameLoader(gameDate: LocalDate) {
      val intent = Intent(this, GameLoaderActivity::class.java)
      intent.putGameDateExtra(gameDate)
      startActivity(intent)
      finish()
   }
}
