package com.letstwinkle.freebee.screens.picker

import androidx.compose.foundation.layout.*
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.compose.MyAppTheme
import com.letstwinkle.freebee.screens.BackNavigator
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GamePickerScreen(backNavigator: BackNavigator, pickerNavigator: GamePickerNavigator?) {
   MyAppTheme {
      Scaffold(topBar = {
         CenterAlignedTopAppBar(
            title = { Text("Choose a Game") },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = backNavigationButton(backNavigator::goBack),
            )
      }) {
         GamePicker(Modifier.padding(it), pickerNavigator)
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GamePicker(modifier: Modifier = Modifier, pickerNavigator: GamePickerNavigator?) {
   val pickerViewModel = viewModel { GamePickerViewModel(repository()) }
   val isDatePickerOpen = rememberSaveable { mutableStateOf(false) }
   
   Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
      Text("Games are created by the New York Times. Choose a date to open the game from that date.", style = bodyStyle)
      Row(Modifier.align(Alignment.CenterHorizontally)) { 
         Button({ isDatePickerOpen.value = true }) {
            Text(formatGameDateToDisplay(pickerViewModel.selectedDate.value))
         }
         Button({ }, enabled = !pickerViewModel.isChoosingRandomDate.value) {
            Text("Go!")
         }
      }
      Text("At this time, we can't preview the game for you before you open it. Maybe in the future!", style = footnoteStyle)
      Text("Or…", Modifier.align(Alignment.CenterHorizontally), style = headlineStyle)
      Button({ 
         pickerViewModel.viewModelScope.launch { 
            pickerViewModel.chooseRandomDate() 
            pickerNavigator?.openGameLoader(pickerViewModel.selectedDate.value)
         } },
         Modifier.align(Alignment.CenterHorizontally),
         enabled = !pickerViewModel.isChoosingRandomDate.value
      ) {
         Text("Open a random date")
      }
   }
   
   if (isDatePickerOpen.value) {
      val datePickerState = rememberDatePickerState(
         initialSelectedDateMillis = pickerViewModel.selectedDate.value
            .atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
         selectableDates = pickerViewModel.selectableDates
      )
      val closeDatePicker = { 
         isDatePickerOpen.value = false
         pickerViewModel.updateSelectedDate(datePickerState.selectedDateMillis)
      }
      
      DatePickerDialog(
         closeDatePicker,
         {
            TextButton(closeDatePicker) { Text("OK") }
         },
      ) {
         DatePicker(datePickerState)
      }
   }
}
