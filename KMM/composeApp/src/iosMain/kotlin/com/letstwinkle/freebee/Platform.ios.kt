package com.letstwinkle.freebee

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.*
import com.letstwinkle.freebee.database.*
import freebee.composeapp.generated.resources.Res
import freebee.composeapp.generated.resources.chevron_back
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toNSDateComponents
import org.jetbrains.compose.resources.vectorResource
import platform.CoreData.NSManagedObjectID
import platform.Foundation.*
import platform.Foundation.NSCalendar.Companion.currentCalendar
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
   override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

typealias iOSRepository = FreeBeeRepository<NSManagedObjectID, Game, GameWithWords>

actual fun getPlatform(): Platform = IOSPlatform()

actual fun formatGameDateToDisplay(date: LocalDate): String {
   val dateComponents = date.toNSDateComponents()
   dateComponents.calendar = currentCalendar
   return dateComponents.date?.let {
      gameDateDisplayFormatter.stringFromDate(it)
   } ?: "Error"
}

val gameDateDisplayFormatter: NSDateFormatter = run {
   val df = NSDateFormatter()
   df.timeStyle = NSDateFormatterNoStyle
   df.dateStyle = NSDateFormatterMediumStyle
   df
}

fun repository(): iOSRepository {
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
