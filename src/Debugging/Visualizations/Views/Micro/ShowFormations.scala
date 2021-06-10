package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowFormations extends View {
  override def renderMap(): Unit = {
    With.squads.all.foreach(_.formation.foreach(_.placements.foreach(p => {
      val unit = p._1
      val slot = p._2
      val c = unit.unitClass
      DrawMap.line(p._1.pixel, slot, Colors.NeonBlue)
      DrawMap.box(slot.subtract(c.dimensionLeft, c.dimensionUp), slot.add(c.dimensionRight, c.dimensionDown), Colors.NeonBlue)
    })))
  }
}
