package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Micro.Coordination.Pushing.PushPriority
import Utilities.ByOption

object ShowPushes extends View {
  override def renderMap(): Unit = {
    With.units.ours.view.filter(_.canMove).foreach(u => With.coordinator.pushes.get(u).foreach(p => {
      val force = p.force(u)
      if (force.nonEmpty) {
        val color = PushPriority.color(p.priority)
        DrawMap.arrow(
          ByOption.minBy(p.tiles.map(_.pixelCorners.minBy(u.pixelDistanceSquared)))(u.pixelDistanceSquared).getOrElse(u.pixelCenter),
          u.pixelCenter,
          color)
        DrawMap.arrow(u.pixelCenter, u.pixelCenter.radiateRadians(force.get.radians, 64), color)
      }
    }))
    With.coordinator.pushes.all.foreach(_.draw())
    With.coordinator.pushes.all.foreach(_.drawTiles())
  }
}
