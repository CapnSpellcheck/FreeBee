//
//  Router.swift
//  iosApp
//
//  Created by G on 12/7/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import UIKit
import ComposeApp
import CoreData
import FreeBeeData

final class Router {
   enum RestorationIdentifiers: String {
      case root
      case list
      case gamePicker
      case gameLoader
      case gameView
      case statistics
   }
   
   static let shared = Router()
   
   let navController = ComposeNavigationController()
   var currentActivity: NSUserActivity
   private lazy var weakNavigator: WeakNavigator = WeakNavigator(router: self)
   
   private init() {
      currentActivity = NSUserActivity(activityType: SceneActivity.gameIndex.activityType)
      installRootController()
   }
   
   func restore(activity: NSUserActivity) -> Bool {
      var viewControllersToPush: Array<UIViewController> = []
      let gameDate: LocalDate? = (activity.userInfo?[SceneActivityKeys.gameDate] as? Date)
         .map {
            ConvertersKt.toKotlinInstant($0)
               .toLocalDateTime(timeZone: ComposeApp.TimeZone.companion.UTC)
               .date
         }
      
      switch activity.activityType {
      case SceneActivity.gamePicker.activityType:
         let viewController = ViewControllersKt.GamePickerViewController(navigator: weakNavigator)
         viewControllersToPush.append(viewController)
      case SceneActivity.gameLoader.activityType:
         guard let gameDate else {
            assertionFailure("State restoration: couldn't find game date in activity's userInfo")
            return false
         }
         let viewController = ViewControllersKt.GameLoaderViewController(gameDate: gameDate, navigator: weakNavigator)
         viewControllersToPush.append(viewController)
      case SceneActivity.game.activityType:
         let enteredLetters = activity.userInfo?[SceneActivityKeys.gameEnteredLetters] as? String
         let gameObjectURL: URL? = activity.userInfo?[SceneActivityKeys.gameURL] as? URL
         let gameObjectID: NSManagedObjectID? = gameObjectURL.flatMap {
            CoreDataDatabase.companion.shared.container.persistentStoreCoordinator.managedObjectID(forURIRepresentation: $0)
         }
         guard let gameDate, let gameObjectID else {
            assertionFailure("State restoration: couldn't find game date in activity's userInfo")
            return false
         }
         if let enteredLetters {
            (CoreDataDatabase.companion.shared.container.viewContext
               .object(with: gameObjectID) as? CDGameProgress)?.currentWord = enteredLetters
         }
         viewControllersToPush.append(gameViewController(gameID: gameObjectID, gameDate: gameDate))
      default:
         return false
      }
      navController.viewControllers = navController.viewControllers + viewControllersToPush
      return true
   }
   
   func showGamePicker() {
      let viewController = ViewControllersKt.GamePickerViewController(navigator: weakNavigator)
      navController.pushViewController(viewController, animated: true)
      currentActivity = NSUserActivity(activityType: SceneActivity.gamePicker.activityType)
   }
   
   func showGameLoader(gameDate: LocalDate) {
      let viewController = ViewControllersKt.GameLoaderViewController(gameDate: gameDate, navigator: weakNavigator)
      navController.replaceTopmost(with: viewController)
      currentActivity = NSUserActivity(activityType: SceneActivity.gameLoader.activityType)
      currentActivity.userInfo = [
         SceneActivityKeys.gameDate: gameDate.atStartOfDayIn(timeZone: ComposeApp.TimeZone.companion.UTC).toNSDate()
      ]
   }
   
   func showStatistics() {
      let viewController = ViewControllersKt.StatisticsViewController(navigator: weakNavigator)
      navController.pushViewController(viewController, animated: true)
   }
   
   func goBack() {
      navController.popViewController(animated: true)
   }
   
   
   func openGame(game: Game) {
      openGame(gameDate: game.date, gameID: game.uniqueID)
   }
   
   func openGame(gameDate: LocalDate, gameID: NSManagedObjectID, replacingTopmost: Bool = false) {
      let viewController = gameViewController(gameID: gameID, gameDate: gameDate)
      if replacingTopmost {
         navController.replaceTopmost(with: viewController, animated: false)
      } else {
         navController.pushViewController(viewController, animated: true)
      }
      
      let gameDate = gameDate.atStartOfDayIn(timeZone: ComposeApp.TimeZone.companion.UTC)
      currentActivity = NSUserActivity(activityType: SceneActivity.game.activityType)
      currentActivity.userInfo = [
         SceneActivityKeys.gameDate: gameDate.toNSDate(),
         SceneActivityKeys.gameURL: gameID.uriRepresentation() as NSURL,
      ]
   }
   
   private func installRootController() {
      let rootVC = ViewControllersKt.GameListViewController(navigator: weakNavigator)
      navController.viewControllers = [rootVC]
   }
   
   private func gameViewController(gameID: NSManagedObjectID, gameDate: LocalDate) -> UIViewController {
      ViewControllersKt.GameViewController(gameID: gameID, gameDate: gameDate, navigator: weakNavigator)
   }
   
}

class WeakNavigator: GameListNavigator, BackNavigator, GamePickerNavigator_iOS,
                        GameLoaderNavigator_iOS {
   weak var router: Router?
   
   init(router: Router) {
      self.router = router
   }
   
   func showStatistics() { router?.showStatistics() }
   func openGame(game: Game) { router?.openGame(game: game) }
   func openGame(gameDate: LocalDate, gameID: NSManagedObjectID) {
      router?.openGame(gameDate: gameDate, gameID: gameID, replacingTopmost: true)
   }
   func openGamePicker() { router?.showGamePicker() }
   func openGameLoader(gameDate: LocalDate) { router?.showGameLoader(gameDate: gameDate) }
   func goBack() { router?.goBack() }
}
