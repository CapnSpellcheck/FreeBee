package com.letstwinkle.freebee

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.letstwinkle.freebee.database.IGame
import com.letstwinkle.freebee.database.android.Game
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
    
    override fun openGame(game: IGame) {
        // this is a blemish, but will do for now
        if (game is Game) {
            val intent = Intent(this, GameActivity::class.java)
            intent.putGameExtra(game)
            startActivity(intent)
        } else {
            error("game should be a com.letstwinkle.freebee.database.android.Game")
        }
    }
    
    override fun openGamePicker() {
    }
}
