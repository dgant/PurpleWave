package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowZonePaths extends DebugView {
  
  override def renderMap(): Unit = {
    With.geography.startLocations.foreach(start1 =>
      With.geography.startLocations.foreach(start2 =>
        if (start1 != start2) {
          val path = With.paths.zonePath(start1.zone, start2.zone)
          if (path.isDefined) {
            path.get.steps.foreach(pathNode =>
              DrawMap.line(
                pathNode.from.centroid.center,
                pathNode.to.centroid.center))
          }
        }
      ))
  }
}
