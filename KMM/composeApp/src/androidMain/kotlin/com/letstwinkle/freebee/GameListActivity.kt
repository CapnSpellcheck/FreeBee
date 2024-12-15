package com.letstwinkle.freebee

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.letstwinkle.freebee.database.Game
import com.letstwinkle.freebee.screens.root.GameListNavigator
import com.letstwinkle.freebee.screens.root.GameListScreen

class GameListActivity : ComponentActivity(), GameListNavigator {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GameListScreen(navigator = this)
        }
    }
    
    override fun showStatistics() {
        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
    }
    
    override fun openGame(game: Game) {
       val intent = Intent(this, GameActivity::class.java)
       intent.putGameExtra(game)
       startActivity(intent)
    }
    
    override fun openGamePicker() {
       val intent = Intent(this, GamePickerActivity::class.java)
       startActivity(intent)
    }
}
