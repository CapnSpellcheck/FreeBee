package com.letstwinkle.freebee.screens.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.letstwinkle.freebee.groupedTableBackgroundColor
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GameList(viewModel: GameListViewModel) {
   val games = viewModel.gamesFlow.collectAsState().value
   Scaffold(topBar = {
      TopAppBar(
         title = { Text("Games") },
         actions = {
            IconButton({ }) {
               Icon(contentDescription = "Statistics", painter = painterResource("chart-bar-xaxis.xml"))
            }
         })
   }) {
      Column(modifier = Modifier.fillMaxSize()) {
         LazyColumn(
            modifier = Modifier.fillMaxHeight().background(groupedTableBackgroundColor)
         ) {
            items(games) { game ->
               Row {
                  Text(game.otherLetters.uppercase())
               }
            }
         }
         Spacer(Modifier.fillMaxHeight())
         Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Sponsor me")
            Text("I appreciate tips! The only social payment platform I'm on is PayPal. Feel free to send me a gift.")
            TextButton({ }) {
               Text("Open PayPal")
            }
         }
      }
   }
}