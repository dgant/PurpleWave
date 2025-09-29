package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Utilities.?

object ShowFlowField extends DebugView {

  override def renderMap(): Unit = {
    val tile = With.units.selected.headOption.map(_.tile).getOrElse(With.geography.home)
    val zone = tile.zone

    With.viewport.areaTiles.tiles.foreach(t => {
      val f = t.flowTo(tile) * 16
      //val f = t.slowGroundDirectionTo(tile) * 16
      if (f.lengthSquared > 0) {
        DrawMap.arrow(t.center, t.center.add(f.x.toInt, f.y.toInt), ?(t.walkable, Colors.NeonGreen, Colors.NeonRed))
      }
    })
  }
}
