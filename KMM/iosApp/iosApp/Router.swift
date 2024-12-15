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
   
   private init() {
      currentActivity = NSUserActivity(activityType: SceneActivity.gameIndex.activityType)
      // That method creates a weak ref to Router.shared, so async it
      DispatchQueue.main.async {
         self.installRootController()
      }
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
         break
//         viewControllersToPush.append(createHostingController(view: GamePicker()))
      case SceneActivity.gameLoader.activityType:
         guard let gameDate else {
            assertionFailure("State restoration: couldn't find game date in activity's userInfo")
            return false
         }
//         viewControllersToPush.append(createHostingController(view: GameLoaderView(gameDate: gameDate)))
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
   
//   func showGameLoader(date: Date) {
//      let viewController = createHostingController(view: GameLoaderView(gameDate: date))
//      navController.replaceTopmost(with: viewController)
//      currentActivity = NSUserActivity(activityType: SceneActivity.gameLoader.activityType)
//      currentActivity.userInfo = [SceneActivityKeys.gameDate: date as NSDate]
//   }
   
   func showStatistics() {
      let viewController = ViewControllersKt.StatisticsViewController(navigator: weakNavigator)
      navController.pushViewController(viewController, animated: true)
   }
   
   func goBack() {
      navController.popViewController(animated: true)
   }
   
   func openGame(game: Game, replacingTopmost: Bool = false) {
      (CoreDataDatabase.companion.shared.container.viewContext
         .object(with: game.uniqueID) as? CDGameProgress)?.addToEnteredWords(CDEnteredWord(context: CoreDataDatabase.companion.shared.container.viewContext, string: "word"))
      let viewController = gameViewController(gameID: game.uniqueID, gameDate: game.date)
      if replacingTopmost {
         navController.replaceTopmost(with: viewController, animated: false)
      } else {
         navController.pushViewController(viewController, animated: true)
      }
      
      let gameDate = game.date.atStartOfDayIn(timeZone: ComposeApp.TimeZone.companion.UTC).toNSDate()
      currentActivity = NSUserActivity(activityType: SceneActivity.game.activityType)
      currentActivity.userInfo = [SceneActivityKeys.gameDate: gameDate]
   }
   
   private func installRootController() {
      let rootVC = ViewControllersKt.GameListViewController(navigator: weakNavigator)
      navController.viewControllers = [rootVC]
   }
   
   private func gameViewController(gameID: NSManagedObjectID, gameDate: LocalDate) -> UIViewController {
      ViewControllersKt.GameViewController(gameID: gameID, gameDate: gameDate, navigator: weakNavigator)
   }

}

class WeakNavigator: GameListNavigator, BackNavigator, GamePickerNavigator_iOS {
   func openGameLoader(gameDate: LocalDate) {
      
   }
   
   weak var router = Router.shared
   
   func showStatistics() { router?.showStatistics() }
   func openGame(game: Game) { router?.openGame(game: game) }
   func openGamePicker() { router?.showGamePicker() }
   func goBack() { router?.goBack() }
}

let weakNavigator = WeakNavigator()
