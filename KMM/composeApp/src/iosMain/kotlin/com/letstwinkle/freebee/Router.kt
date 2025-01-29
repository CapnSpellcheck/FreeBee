package com.letstwinkle.freebee

import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.screens.BackNavigator
import com.letstwinkle.freebee.screens.loader.GameLoaderNavigator
import com.letstwinkle.freebee.screens.picker.GamePickerNavigator
import com.letstwinkle.freebee.screens.root.GameListNavigator
import kotlinx.datetime.*
import platform.Foundation.*
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi

interface Routing : BackNavigator, GameLoaderNavigator, GameListNavigator, GamePickerNavigator {
   fun restore(activity: NSUserActivity): Boolean
}

@Suppress("unused")
object Router : Routing {
   @Suppress("MemberVisibilityCanBePrivate")
   val navigationController = FreeBeeNavigationController()
   @Suppress("MemberVisibilityCanBePrivate")
   var currentActivity: NSUserActivity; private set
   
   init {
      currentActivity = NSUserActivity(UserActivity.GameList.activityType)
      installRootController()
   }
   
   @OptIn(ExperimentalNativeApi::class)
   override fun restore(activity: NSUserActivity): Boolean {
      val gameDate: LocalDate? =
         (activity.userInfo?.get(UserActivityKeys.gameDate) as? NSDate)
            ?.toKotlinInstant()
            ?.toLocalDateTime(TimeZone.UTC)?.date
      
      val pushViewControllers: List<UIViewController> = when (activity.activityType) {
         UserActivity.GameList.activityType -> 
            emptyList()
         UserActivity.Picker.activityType ->
            listOf(GamePickerViewController(this))
         UserActivity.Loader.activityType -> {
            if (gameDate == null) {
               assert(false) { "State restoration: couldn't find game date in activity's userInfo" }
               return false
            }
            listOf(GameLoaderViewController(gameDate, this))
         }
         UserActivity.Game.activityType -> {
            // TODO: currentWord
//            val enteredLetters = activity.userInfo?[UserActivityKeys.gameEnteredLetters] as? String
            val gameObjectURL = activity.userInfo?.get(UserActivityKeys.gameURL) as? NSURL
            val gameObjectID = gameObjectURL?.let { url ->
               CoreDataDatabase.shared.container.persistentStoreCoordinator.managedObjectIDForURIRepresentation(url)
            }
            if (gameDate == null || gameObjectID == null) {
               assert(false) { "State restoration: couldn't find game date in activity's userInfo" }
               return false
            }

//            if let enteredLetters {
//               (CoreDataDatabase.companion.shared.container.viewContext
//                  .object(with: gameObjectID) as? CDGameProgress)?.currentWord = enteredLetters
//            }
            listOf(gameViewController(gameObjectID, gameDate))
         }
         
         else -> emptyList()
      }
      navigationController.viewControllers += pushViewControllers
      return true
   }
   
   override fun showStatistics() {
      val viewController = StatisticsViewController(this)
      navigationController.pushViewController(viewController, true)
   }
   
   override fun openGame(game: Game) {
      openGame(game.date, game.uniqueID, false)
   }
   
   override fun openGame(gameDate: LocalDate, gameID: EntityIdentifier) {
      openGame(gameDate, gameID, true)
   }
   
   private fun openGame(gameDate: LocalDate, gameID: EntityIdentifier, replacingTopmost: Boolean) {
      val viewController = gameViewController(gameID, gameDate)
      if (replacingTopmost) {
         navigationController.replaceTopmost(viewController, false)
      } else {
         navigationController.pushViewController(viewController, true)
      }
      
      val gameDateUTC = gameDate.atStartOfDayIn(TimeZone.UTC)
      currentActivity = NSUserActivity(UserActivity.Game.activityType)
      currentActivity.userInfo = mapOf(
         UserActivityKeys.gameDate to gameDateUTC.toNSDate(),
         UserActivityKeys.gameURL to gameID.URIRepresentation(),
      )
   }
   
   override fun openGamePicker() {
      val viewController = GamePickerViewController(this)
      navigationController.pushViewController(viewController, true)
      currentActivity = NSUserActivity(UserActivity.Picker.activityType)
   }
   
   override fun openGameLoader(gameDate: LocalDate) {
      openGameLoader(gameDate) { navigationController.replaceTopmost(it) }
   }
   
   override fun goBack() {
      navigationController.popViewControllerAnimated(true)
   }
   
   fun openToday() {
      val todaysDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
      openGameLoader(todaysDate) {
         navigationController.popToRootViewControllerAnimated(false)
         navigationController.pushViewController(it, true)
      }
   }
   
   private fun installRootController() {
      val rootVC = GameListViewController(this)
      navigationController.setViewControllers(listOf(rootVC))
   }
   
   private fun openGameLoader(gameDate: LocalDate, navigationHandler: (UIViewController) -> Unit) {
      val viewController = GameLoaderViewController(gameDate, this)
      navigationHandler(viewController)
      currentActivity = NSUserActivity(UserActivity.Loader.activityType)
      currentActivity.userInfo = mapOf(
         UserActivityKeys.gameDate to gameDate.atStartOfDayIn(TimeZone.UTC).toNSDate()
      )
   }
   
   private fun gameViewController(gameID: EntityIdentifier, gameDate: LocalDate): UIViewController =
      GameViewController(gameID, gameDate, this)
}
