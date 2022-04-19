package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Views.DebugView
import Placement.JBWEBWrapper

object ShowBWEB extends DebugView {

  override def renderMap(): Unit = {
    JBWEBWrapper.draw()
  }
}
