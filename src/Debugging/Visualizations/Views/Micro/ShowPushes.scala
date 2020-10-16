package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Utilities.ByOption

object ShowPushes extends View {
  override def renderMap(): Unit = {
    With.units.ours.view.filter(_.canMove).foreach(u => With.coordinator.pushes.get(u).foreach(push => {
      val force = push.force(u)
      if (force.nonEmpty) {
        DrawMap.arrow(
          ByOption.minBy(push.tiles.map(_.pixelCorners.minBy(u.pixelDistanceSquared)))(u.pixelDistanceSquared).getOrElse(u.pixelCenter),
          u.pixelCenter,
          push.priority.color)
        DrawMap.arrow(u.pixelCenter, u.pixelCenter.radiateRadians(force.get.radians, 64), push.priority.color)
      }
    }))
    With.coordinator.pushes.all.foreach(_.draw())
    With.coordinator.pushes.all.foreach(_.drawTiles())
  }
}
