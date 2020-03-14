package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.SpecificPoints
import bwapi.Color

object ShowIntelligence extends View {

  override def renderMap(): Unit = {
    val bases = With.scouting.mostIntriguingBases()
    bases.indices.foreach(i => {
      val base = bases(i)
      val pixel = base.townHallTile.pixelCenter
      val value = 192 - (192.0 * i / bases.size).toInt
      val color = new Color(value, value, 0)
      val label = "#" + (i + 1) + ": " + base.name + ", " + base.zone.name
      DrawMap.line(SpecificPoints.middle, pixel, color)
      DrawMap.label(label, pixel, true, color)
      DrawMap.label(label, SpecificPoints.middle.project(pixel, 108), true, color)
    })
  }
}
