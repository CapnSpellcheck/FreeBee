package com.letstwinkle.freebee

import android.content.Context
import android.os.Build
import android.os.Parcel
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.database.android.RoomDatabase
import kotlinx.datetime.*
import kotlinx.parcelize.Parceler
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AndroidPlatform : Platform {
   override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

typealias AndroidRepository = FreeBeeRepository<Long, Game, GameWithWords>

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun formatGameDateToDisplay(date: LocalDate): String = gameDateDisplayFormatter.format(date.toJavaLocalDate())

val gameDateDisplayFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
   FormatStyle.MEDIUM
)
fun repository(): AndroidRepository {
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
