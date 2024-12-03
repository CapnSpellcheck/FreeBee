package com.letstwinkle.freebee

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Typeface
import com.letstwinkle.freebee.database.CoreDataDatabase
import com.letstwinkle.freebee.database.CovariantFreeBeeRepository
import kotlinx.cinterop.*
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import org.jetbrains.skia.*
import platform.Foundation.*
import platform.UIKit.UIDevice
import platform.posix.memcpy
import org.jetbrains.skia.Typeface as SkTypeface

class IOSPlatform: Platform {
   override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun formatGameDateToDisplay(date: Instant): String =
   gameDateDisplayFormatter.stringFromDate(date.toNSDate())

val gameDateDisplayFormatter: NSDateFormatter = run {
   val df = NSDateFormatter()
   df.locale = NSLocale("en_US_POSIX")
   df.timeZone = NSTimeZone.timeZoneWithAbbreviation("UTC")!!
   df.timeStyle = NSDateFormatterNoStyle
   df.dateStyle = NSDateFormatterMediumStyle
   df
}

actual fun repository(): CovariantFreeBeeRepository {
   return CoreDataDatabase.shared
}

@OptIn(BetaInteropApi::class)
actual fun gameLettersFontFamily() = FontFamily(
   Typeface(
      SkTypeface.makeFromName(
         "IBMPlexSans-Medium.ttf",
         FontStyle(FontWeight.MEDIUM, FontWidth.NORMAL, FontSlant.UPRIGHT)
      )
   )
)
