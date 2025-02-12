@file:OptIn(ExperimentalTestApi::class)

package com.letstwinkle.freebee.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.letstwinkle.freebee.util.TestRepository
import kotlin.test.Test

class GameListTest {
   
   @Test fun foo() = runComposeUiTest { 
      val repository = TestRepository()
   }
}
