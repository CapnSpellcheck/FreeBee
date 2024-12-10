package com.letstwinkle.freebee

import androidx.compose.ui.window.ComposeUIViewController
import com.letstwinkle.freebee.database.EntityIdentifier
import com.letstwinkle.freebee.screens.StatisticsScreen
import com.letstwinkle.freebee.screens.game.BackNavigator
import com.letstwinkle.freebee.screens.game.GameScreen
import com.letstwinkle.freebee.screens.root.GameListNavigator
import com.letstwinkle.freebee.screens.root.GameListScreen
import kotlinx.datetime.Instant

fun GameListViewController(navigator: GameListNavigator) = ComposeUIViewController { GameListScreen(navigator) }

fun GameViewController(gameID: EntityIdentifier, gameDate: Instant, navigator: BackNavigator) = 
   ComposeUIViewController { GameScreen(gameID, gameDate, navigator) }

fun StatisticsViewController() = ComposeUIViewController { StatisticsScreen() }
