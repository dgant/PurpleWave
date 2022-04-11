package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Views.View
import Information.Geography.JBWEBWrapper

object ShowBWEB extends View {

  override def renderMap(): Unit = {
    JBWEBWrapper.draw()
  }
}
