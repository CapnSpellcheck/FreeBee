package com.letstwinkle.freebee

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

class PreviewPainterProvider : PainterProvider {
   @Composable
   override fun provide(r: PainterProvider.Resource): Painter {
      @DrawableRes val id: Int = when (r) {
         PainterProvider.Resource.ChartBarXaxis -> R.drawable.chart_bar_xaxis
         PainterProvider.Resource.Chevron -> R.drawable.chevron_right
      }
      return painterResource(id)
   }
}