package com.letstwinkle.freebee

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.letstwinkle.freebee.screens.root.GameList
import com.letstwinkle.freebee.screens.root.GameListViewModel

@Composable
fun App() {
   MaterialTheme {
      val gameListViewModel = GameListViewModel(repository())
      GameList(gameListViewModel)
   }
}