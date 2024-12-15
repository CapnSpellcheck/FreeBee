package com.letstwinkle.freebee

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.ComposeUIViewController
import com.letstwinkle.freebee.database.EntityIdentifier
import com.letstwinkle.freebee.screens.BackNavigator
import com.letstwinkle.freebee.screens.StatisticsScreen
import com.letstwinkle.freebee.screens.game.GameScreen
import com.letstwinkle.freebee.screens.picker.GamePickerNavigator
import com.letstwinkle.freebee.screens.picker.GamePickerScreen
import com.letstwinkle.freebee.screens.root.GameListNavigator
import com.letstwinkle.freebee.screens.root.GameListScreen
import kotlinx.datetime.LocalDate

fun GameListViewController(navigator: GameListNavigator) = ComposeUIViewController { GameListScreen(navigator) }

@OptIn(ExperimentalMaterial3Api::class)
fun GameViewController(gameID: EntityIdentifier, gameDate: LocalDate, navigator: BackNavigator) = 
   ComposeUIViewController { GameScreen(gameID, gameDate, navigator) }

fun StatisticsViewController(navigator: BackNavigator) = ComposeUIViewController { StatisticsScreen(navigator) }

interface GamePickerNavigator_iOS: GamePickerNavigator, BackNavigator
fun GamePickerViewController(navigator: GamePickerNavigator_iOS) = ComposeUIViewController { GamePickerScreen(navigator, navigator) }
