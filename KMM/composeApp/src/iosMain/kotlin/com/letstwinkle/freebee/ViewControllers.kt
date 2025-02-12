package com.letstwinkle.freebee

import androidx.compose.ui.window.ComposeUIViewController
import com.letstwinkle.freebee.database.Game
import com.letstwinkle.freebee.screens.BackNavigator
import com.letstwinkle.freebee.screens.StatisticsScreen
import com.letstwinkle.freebee.screens.game.GameScreen
import com.letstwinkle.freebee.screens.loader.GameLoaderScreen
import com.letstwinkle.freebee.screens.picker.GamePickerScreen
import com.letstwinkle.freebee.screens.root.GameListNavigator
import com.letstwinkle.freebee.screens.root.GameListScreen
import kotlinx.datetime.LocalDate
import platform.CoreData.NSManagedObjectID

private typealias Id = NSManagedObjectID

fun GameListViewController(navigator: GameListNavigator<Game>) = 
   ComposeUIViewController { GameListScreen(repository(), navigator) }

fun GameViewController(gameID: Id, gameDate: LocalDate, navigator: BackNavigator) = 
   ComposeUIViewController { GameScreen(repository(), gameID, gameDate, navigator) }

fun StatisticsViewController(navigator: BackNavigator) = 
   ComposeUIViewController { StatisticsScreen(repository(), navigator) }

fun GamePickerViewController(router: Routing) = ComposeUIViewController { 
   GamePickerScreen(repository(), router, router)
}

fun GameLoaderViewController(gameDate: LocalDate, router: Routing) = ComposeUIViewController { 
   GameLoaderScreen(repository(), gameDate, router, router)
}
