package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowPathfinding extends DebugView {

  override def renderMap(): Unit = {
    With.grids.debugPathfinding.drawMap()
  }
}
