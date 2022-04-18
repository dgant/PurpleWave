package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowPlacement extends DebugView {

  override def renderMap(): Unit = {
    With.placement.fits.foreach(_.drawMap())
  }
}
