package com.letstwinkle.freebee

import android.content.Context
import android.os.Build
import android.os.Parcel
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import com.letstwinkle.freebee.database.FreeBeeRepository
import com.letstwinkle.freebee.database.android.RoomDatabase
import kotlinx.datetime.*
import kotlinx.parcelize.Parceler
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class AndroidPlatform : Platform {
   override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun formatGameDateToDisplay(date: LocalDate): String = gameDateDisplayFormatter.format(date.toJavaLocalDate())

val gameDateDisplayFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
   FormatStyle.MEDIUM
)
actual fun repository(): FreeBeeRepository {
   return RoomDatabase.getDatabase(applicationContext)
}

object InstantClassParceler : Parceler<Instant?> {
   override fun create(parcel: Parcel) = parcel.readString()?.let { Instant.parse(it) }
   
   override fun Instant?.write(parcel: Parcel, flags: Int) {
      parcel.writeString(this?.toString())
   }
}

object LocalDateClassParceler : Parceler<LocalDate> {
   override fun create(parcel: Parcel): LocalDate = LocalDate.fromEpochDays(parcel.readInt())
   override fun LocalDate.write(parcel: Parcel, flags: Int) {
      parcel.writeInt(this.toEpochDays())
   }
}

private lateinit var applicationContext: Context

fun setApplicationContext(context: Context) {
   applicationContext = context
}

actual fun backNavigationButton(onClick: () -> Unit): @Composable () -> Unit = {
   IconButton(onClick) {
      Icon(Icons.AutoMirrored.Filled.ArrowBack, "back")
   }
}
