package com.letstwinkle.freebee

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import freebee.composeapp.generated.resources.*
import freebee.composeapp.generated.resources.Res
import freebee.composeapp.generated.resources.chart_bar_xaxis
import org.jetbrains.compose.resources.*

interface PainterProvider {
   @Composable fun provide(r: Resource): Painter
   
   enum class Resource(val resource: DrawableResource) {
      ChartBarXaxis(Res.drawable.chart_bar_xaxis),
      Chevron(Res.drawable.chevron_right),
      ChevronDown(Res.drawable.chevron_down),
      Backspace(Res.drawable.backspace),
      XCircleFill(Res.drawable.x_circle_fill),
      Enter(Res.drawable.enter),
      Rules(Res.drawable.questionmark_circle),
      ChevronBack(Res.drawable.chevron_back),
      Brain(Res.drawable.brain),
      Hint(Res.drawable.light_bulb_max),
      Shuffle(Res.drawable.shuffle),
      Sort(Res.drawable.sort),
      ;
   }
}

class ResourcePainterProvider : PainterProvider {
   @Composable
   override fun provide(r: PainterProvider.Resource): Painter {
      return painterResource(r.resource)
   }
}
