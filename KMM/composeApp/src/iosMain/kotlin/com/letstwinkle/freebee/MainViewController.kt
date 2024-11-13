package com.letstwinkle.freebee

import androidx.compose.ui.window.ComposeUIViewController
import com.letstwinkle.freebee.screens.root.GameListScreen

fun MainViewController() = ComposeUIViewController { GameListScreen(navigator = null) }