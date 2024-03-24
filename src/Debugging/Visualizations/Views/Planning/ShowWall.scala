package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Placement.Access.PlaceLabels.Wall

object ShowWall extends DebugView {

  override def renderMap(): Unit = {
    With.placement.fits
      .view
      .filter(_.template.points.exists(_.requirement.labels.contains(Wall)))
      .foreach(_.drawMap())
  }
}
