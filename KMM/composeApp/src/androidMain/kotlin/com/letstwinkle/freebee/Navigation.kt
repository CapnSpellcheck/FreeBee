package com.letstwinkle.freebee

import androidx.activity.ComponentActivity
import com.letstwinkle.freebee.screens.BackNavigator

fun ComponentActivity.backNavigator(): BackNavigator = object : BackNavigator {
   override fun goBack() {
      finish()
   }
}
