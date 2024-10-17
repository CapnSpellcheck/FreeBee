//
//  Router.swift
//  Free Bee
//
//  Created by G on 10/15/24.
//

import Foundation
import UIKit
import SwiftUI

fileprivate let kGameDateCodingKey = "gameDate"

final class Router: ObservableObject {
   enum RestorationIdentifiers: String {
      case root
      case list
      case gamePicker
      case gameLoader
      case gameView
      case statistics
   }
   
   static let shared = Router()
   
   
   let navController = OrientationNavigationController()
   var currentActivity: NSUserActivity
   
   private init() {
      navController.navigationBar.prefersLargeTitles = true
      currentActivity = NSUserActivity(activityType: SceneActivity.gameIndex.activityType)
      installRootController()
   }
   
   func restore(activity: NSUserActivity) -> Bool {
      var viewControllersToPush: Array<UIViewController> = []
      let gameDate: Date? = activity.userInfo?[SceneActivityKeys.gameDate] as? Date
      let enteredLetters = activity.userInfo?[SceneActivityKeys.gameEnteredLetters] as? String
      
      switch activity.activityType {
      case SceneActivity.gamePicker.activityType:
         viewControllersToPush.append(createHostingController(view: GamePicker()))
      case SceneActivity.gameLoader.activityType:
         guard let gameDate else {
            assertionFailure("State restoration: couldn't find game date in activity's userInfo")
            return false
         }
         viewControllersToPush.append(createHostingController(view: GameLoaderView(gameDate: gameDate)))
      case SceneActivity.game.activityType:
         guard let gameDate else {
            assertionFailure("State restoration: couldn't find game date in activity's userInfo")
            return false
         }
         guard let gameView = createGameView(gameDate: gameDate) else {
            assertionFailure("Can't load game for date \(gameDate)")
            return false
         }
         if let enteredLetters {
            gameView.viewModel.game.progress?.currentWord = enteredLetters
         }
         viewControllersToPush.append(createHostingController(view: gameView, orientations: [.portrait]))
      default:
         return false
      }
      navController.viewControllers = navController.viewControllers + viewControllersToPush
      return true
   }

   func showGamePicker() {
      let viewController = createHostingController(view: GamePicker())
      navController.pushViewController(viewController, animated: true)
      currentActivity = NSUserActivity(activityType: SceneActivity.gamePicker.activityType)
   }
   
   func showGameLoader(date: Date) {
      let viewController = createHostingController(view: GameLoaderView(gameDate: date))
      navController.replaceTopmost(with: viewController)
      currentActivity = NSUserActivity(activityType: SceneActivity.gameLoader.activityType)
      currentActivity.userInfo = [SceneActivityKeys.gameDate: date as NSDate]
   }
   
   func showStatistics() {
      let viewController = createHostingController(view: StatisticsView())
      navController.pushViewController(viewController, animated: true)
   }
   
   func pop() {
      navController.popViewController(animated: true)
   }
   
   func showGame(date: Date, replacingTopmost: Bool = false) {
      guard let gameView = createGameView(gameDate: date) else {
         assertionFailure("Can't load game for date \(date)")
         return
      }
      let viewController = createHostingController(view: gameView, orientations: [.portrait])
      
      if replacingTopmost {
         navController.replaceTopmost(with: viewController, animated: false)
      } else {
         navController.pushViewController(viewController, animated: true)
      }
      currentActivity = NSUserActivity(activityType: SceneActivity.game.activityType)
      currentActivity.userInfo = [SceneActivityKeys.gameDate: date as NSDate]
   }
   
   private func installRootController() {
      let gameView = GameList()
         .environment(\.managedObjectContext, PersistenceController.shared.container.viewContext)
         .environmentObject(self)
      let rootVC = OrientationHostingController(rootView: gameView)
      
      navController.viewControllers = [rootVC]
   }
   
   private func createGameView(gameDate: Date) -> GameView? {
      let view = GameView(gameDate: gameDate, context: PersistenceController.shared.container.viewContext)
      view?.viewModel.onCurrentWordChanged = { [weak self] word in
         self?.currentActivity.userInfo?[SceneActivityKeys.gameEnteredLetters] = word
      }
      return view
   }
   
   private func createHostingController(
      view: some View,
      orientations: UIInterfaceOrientationMask? = nil
   ) -> OrientationHostingController<some View> {
      let newVC = OrientationHostingController(rootView: view.environmentObject(self))
      if let orientations {
         newVC.supportedOrientations = orientations
      }
      return newVC
   }
}

fileprivate class OrientationHostingController<Content: View>: UIHostingController<Content> {
   var supportedOrientations: UIInterfaceOrientationMask = [.all]
   override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
      supportedOrientations
   }
}
