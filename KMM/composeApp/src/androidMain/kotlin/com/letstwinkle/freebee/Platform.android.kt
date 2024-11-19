package com.letstwinkle.freebee

import android.content.Context
import android.os.Build
import android.os.Parcel
import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.database.android.RoomDatabase
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.parcelize.Parceler
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun formatGameDateToDisplay(date: Instant): String = gameDateDisplayFormatter.format(date.toJavaInstant())

val gameDateDisplayFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
    FormatStyle.MEDIUM
).withLocale(Locale("en", "US", "POSIX"))
    .withZone(ZoneId.of("Z"))

actual fun repository(): CovariantFreeBeeRepository {
    return RoomDatabase.getDatabase(applicationContext)
}

object InstantClassParceler : Parceler<Instant?> {
    override fun create(parcel: Parcel) = parcel.readString()?.let { Instant.parse(it) }
    
    override fun Instant?.write(parcel: Parcel, flags: Int) {
        parcel.writeString(this?.toString())
    }
}

private lateinit var applicationContext: Context

fun setApplicationContext(context: Context) {
    applicationContext = context
}
