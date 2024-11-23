package com.letstwinkle.freebee

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

interface PainterProvider {
   @Composable fun provide(r: Resource): Painter
   
   enum class Resource(val resourceName: String) {
      ChartBarXaxis("chart-bar-xaxis.xml"),
      Chevron("chevron-right.xml"),
      ChevronDown("chevron-down.xml"),
      Backspace("backspace.xml"),
      XCircleFill("x-circle-fill.xml"),
      Enter("enter.png"),
      Rules("questionmark-circle.xml"),
      ;
   }
}

class ResourcePainterProvider : PainterProvider {
   @OptIn(ExperimentalResourceApi::class)
   @Composable
   override fun provide(r: PainterProvider.Resource): Painter {
      return painterResource(r.resourceName)
   }
}
