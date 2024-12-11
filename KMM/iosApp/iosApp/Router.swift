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
      let gameDate: Date? = activity.userInfo?[SceneActivityKeys.gameDate] as? Date
      
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
         let gameInstant = gameDate.toInstant()
         viewControllersToPush.append(gameViewController(gameID: gameObjectID, gameInstant: gameInstant))
      default:
         return false
      }
      navController.viewControllers = navController.viewControllers + viewControllersToPush
      return true
   }

//   func showGamePicker() {
//      let viewController = createHostingController(view: GamePicker())
//      navController.pushViewController(viewController, animated: true)
//      currentActivity = NSUserActivity(activityType: SceneActivity.gamePicker.activityType)
//   }
   
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
      let viewController = gameViewController(gameID: game.uniqueID, gameInstant: game.date)
      if replacingTopmost {
         navController.replaceTopmost(with: viewController, animated: false)
      } else {
         navController.pushViewController(viewController, animated: true)
      }
      
      let gameDate = game.date.toNSDate()
      currentActivity = NSUserActivity(activityType: SceneActivity.game.activityType)
      currentActivity.userInfo = [SceneActivityKeys.gameDate: gameDate]
   }
   
   private func installRootController() {
      let rootVC = ViewControllersKt.GameListViewController(navigator: weakNavigator)
      navController.viewControllers = [rootVC]
   }
   
   private func gameViewController(gameID: NSManagedObjectID, gameInstant: Instant) -> UIViewController {
      ViewControllersKt.GameViewController(gameID: gameID, gameDate: gameInstant, navigator: weakNavigator)
   }

}

class WeakNavigator: GameListNavigator, BackNavigator {
   weak var router = Router.shared
   
   func showStatistics() { router?.showStatistics() }
   func openGame(game: Game) { router?.openGame(game: game) }
   func openGamePicker() { }
   func goBack() { router?.goBack() }
}

let weakNavigator = WeakNavigator()
