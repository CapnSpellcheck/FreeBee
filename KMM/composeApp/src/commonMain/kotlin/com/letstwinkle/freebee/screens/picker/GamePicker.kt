package com.letstwinkle.freebee.screens.picker

import androidx.compose.foundation.layout.*
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.compose.MyAppTheme
import com.letstwinkle.freebee.compose.iOSStyleFilledButton
import com.letstwinkle.freebee.database.AnyFreeBeeRepository
import com.letstwinkle.freebee.screens.BackNavigator
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GamePickerScreen(
   repository: AnyFreeBeeRepository,
   backNavigator: BackNavigator,
   pickerNavigator: GamePickerNavigator?,
) {
   MyAppTheme {
      Scaffold(topBar = {
         CenterAlignedTopAppBar(
            title = { Text("Choose a Game") },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = backNavigationButton(backNavigator::goBack),
            )
      }) {
         GamePicker(repository, Modifier.padding(it), pickerNavigator = pickerNavigator)
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GamePicker(
   repository: AnyFreeBeeRepository,
   modifier: Modifier = Modifier,
   viewModel: GamePickerViewModel = viewModel { GamePickerViewModel(repository) },
   pickerNavigator: GamePickerNavigator? = null,
) {
   val isDatePickerOpen = rememberSaveable { mutableStateOf(false) }
   
   Column(
      modifier.fillMaxSize().padding(horizontal = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
   ) {
      Text("Games are created by the New York Times. Choose a date to open the game from that date.", style = bodyStyle)
      Row(
         Modifier.align(Alignment.CenterHorizontally),
         horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) { 
         iOSStyleFilledButton({ isDatePickerOpen.value = true }) {
            Text(formatGameDateToDisplay(viewModel.selectedDate.value), style = bodyStyle)
         }
         iOSStyleFilledButton(
            { pickerNavigator?.openGameLoader(viewModel.selectedDate.value) },
            enabled = !viewModel.isChoosingRandomDate.value,
            prominent = true
         ) {
            Text("Go!", style = bodyStyle)
         }
      }
      Text("At this time, we can't preview the game for you before you open it. Maybe in the future!", Modifier.padding(top = 8.dp, bottom = 40.dp), style = footnoteStyle)
      Text("Or…", Modifier.align(Alignment.CenterHorizontally), style = headlineStyle)
      iOSStyleFilledButton({ 
         viewModel.viewModelScope.launch { 
            viewModel.chooseRandomDate() 
            pickerNavigator?.openGameLoader(viewModel.selectedDate.value)
         } },
         Modifier.align(Alignment.CenterHorizontally),
         enabled = !viewModel.isChoosingRandomDate.value, 
      ) {
         Text("Open a random date", style = bodyStyle)
      }
   }
   
   if (isDatePickerOpen.value) {
      val datePickerState = rememberDatePickerState(
         initialSelectedDateMillis = viewModel.selectedDate.value
            .atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
         selectableDates = viewModel.selectableDates
      )
      val closeDatePicker = { 
         isDatePickerOpen.value = false
         viewModel.updateSelectedDate(datePickerState.selectedDateMillis)
      }
      
      DatePickerDialog(
         closeDatePicker,
         {
            Button(closeDatePicker) { Text("OK") }
         },
      ) {
         DatePicker(datePickerState)
      }
   }
}
