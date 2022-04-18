package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Views.DebugView
import Information.Geography.JBWEBWrapper

object ShowBWEB extends DebugView {

  override def renderMap(): Unit = {
    JBWEBWrapper.draw()
  }
}
