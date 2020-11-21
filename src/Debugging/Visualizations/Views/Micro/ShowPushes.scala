package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Micro.Coordination.Pushing.Push
import Utilities.ByOption

import scala.collection.mutable

object ShowPushes extends View {
  override def renderMap(): Unit = {
    val activePushes = new mutable.HashSet[Push]
    With.units.ours.view.filter(_.canMove).foreach(u => With.coordinator.pushes.get(u).foreach(push => {
      if (push.priority >= u.agent.priority) {
        val force = push.force(u)
        if (force.nonEmpty) {
          activePushes += push
          DrawMap.arrow(
            ByOption.minBy(push.tiles.map(_.pixelCorners.minBy(u.pixelDistanceSquared)))(u.pixelDistanceSquared).getOrElse(u.pixelCenter),
            u.pixelCenter,
            push.priority.color)
          DrawMap.arrow(u.pixelCenter, u.pixelCenter.radiateRadians(force.get.radians, 64), push.priority.color)
        }
      }
    }))
    activePushes.foreach(_.drawTiles())
    activePushes.foreach(_.draw())
  }
}
