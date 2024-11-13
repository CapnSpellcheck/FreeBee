package com.letstwinkle.freebee

import android.os.Build
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
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