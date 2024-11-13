package com.letstwinkle.freebee

import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.*
import platform.UIKit.UIDevice

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