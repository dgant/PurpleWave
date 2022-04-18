package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Maff
import Micro.Coordination.Pushing.Push

import scala.collection.mutable

object ShowPushes extends DebugView {
  override def renderMap(): Unit = {
    val activePushes = new mutable.HashSet[Push]
    With.units.ours.view.filter(_.canMove).foreach(u => With.coordinator.pushes.get(u).foreach(push => {
      if (push.priority > u.agent.priority) {
        val force = push.force(u)
        if (force.nonEmpty) {
          activePushes += push
          DrawMap.arrow(
            Maff.minBy(push.tiles.map(_.pixelCorners.minBy(u.pixelDistanceSquared)))(u.pixelDistanceSquared).getOrElse(u.pixel),
            u.pixel,
            push.priority.color)
          DrawMap.arrow(u.pixel, u.pixel.radiateRadians(force.get.radians, 64), push.priority.color)
        }
      }
    }))
    activePushes.foreach(_.drawTiles())
    activePushes.foreach(_.draw())
  }
}
