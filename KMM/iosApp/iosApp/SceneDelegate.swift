//
//  SceneDelegate.swift
//
//  Created by G on 9/27/24.
//

import UIKit
// TODO: put this back into "iOS shared" directory, and add #ifdef for line below
import ComposeApp

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
   var window: UIWindow?
   var connectionShortcutItem: UIApplicationShortcutItem?
   
   func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
      // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
      // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
      // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
      guard let windowScene = (scene as? UIWindowScene) else { return }
      guard NSClassFromString("XCTestObservationCenter") == nil else {
         return
      }
      
      connectionShortcutItem = connectionOptions.shortcutItem          // Save it off for later when we become active.
      
      /// 2. Create a new UIWindow using the windowScene constructor which takes in a window scene.
      let window = UIWindow(windowScene: windowScene)
      
      /// 3. Create a view hierarchy programmatically
      let router = Router.shared
      window.rootViewController = router.navigationController
      
      if let userActivity = session.stateRestorationActivity {
         if router.restore(activity: userActivity) {
            scene.userActivity = userActivity
         }
      }
      
      /// 5. Set the window and call makeKeyAndVisible()
      self.window = window
      window.backgroundColor = UIColor.white
      window.makeKeyAndVisible()
   }
   
   func stateRestorationActivity(for scene: UIScene) -> NSUserActivity? {
      Router.shared.currentActivity
   }
   
   func windowScene(_ windowScene: UIWindowScene, performActionFor shortcutItem: UIApplicationShortcutItem, completionHandler: @escaping (Bool) -> Void) {
      handleShortcutItem(item: shortcutItem)
      completionHandler(true)
   }
   
   func sceneDidDisconnect(_ scene: UIScene) {
      // Called as the scene is being released by the system.
      // This occurs shortly after the scene enters the background, or when its session is discarded.
      // Release any resources associated with this scene that can be re-created the next time the scene connects.
      // The scene may re-connect later, as its session was not necessarily discarded (see `application:didDiscardSceneSessions` instead).
   }
   
   func sceneDidBecomeActive(_ scene: UIScene) {
      // Called when the scene has moved from an inactive state to an active state.
      // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
      if let connectionShortcutItem {
         handleShortcutItem(item: connectionShortcutItem)
      }
      connectionShortcutItem = nil
   }
   
   func sceneWillResignActive(_ scene: UIScene) {
      // Called when the scene will move from an active state to an inactive state.
      // This may occur due to temporary interruptions (ex. an incoming phone call).
   }
   
   func sceneWillEnterForeground(_ scene: UIScene) {
      // Called as the scene transitions from the background to the foreground.
      // Use this method to undo the changes made on entering the background.
   }
   
   func sceneDidEnterBackground(_ scene: UIScene) {
      // Called as the scene transitions from the foreground to the background.
      // Use this method to save data, release shared resources, and store enough scene-specific state information
      // to restore the scene back to its current state.
   }
   
   private func handleShortcutItem(item: UIApplicationShortcutItem) {
      if item.type == "Today" {
         Router.shared.openToday()
      }
   }
}
