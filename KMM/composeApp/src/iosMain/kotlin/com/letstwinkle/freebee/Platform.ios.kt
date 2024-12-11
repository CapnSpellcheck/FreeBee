package com.letstwinkle.freebee

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.*
import com.letstwinkle.freebee.database.CoreDataDatabase
import com.letstwinkle.freebee.database.FreeBeeRepository
import freebee.composeapp.generated.resources.Res
import freebee.composeapp.generated.resources.chevron_back
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import org.jetbrains.compose.resources.vectorResource
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

actual fun repository(): FreeBeeRepository {
   return CoreDataDatabase.shared
}

actual fun backNavigationButton(onClick: () -> Unit): @Composable () -> Unit = {
   TextButton(onClick) {
      val inline = mapOf(
         "chevron" to InlineTextContent(
            Placeholder(10.sp, 16.sp, PlaceholderVerticalAlign.Center)
         ) {
            val imageVector = vectorResource(Res.drawable.chevron_back)
            Image(
               imageVector,
               "",
               Modifier.size(imageVector.defaultWidth / imageVector.defaultHeight * 16.dp, 16.dp),
               colorFilter = ColorFilter.tint(iOSInspiredBlueActionColor),
            )
         }
      )
      Text(buildAnnotatedString {
         appendInlineContent("chevron")
         append(" Back")
      }, inlineContent = inline, color = iOSInspiredBlueActionColor)
   }
}
