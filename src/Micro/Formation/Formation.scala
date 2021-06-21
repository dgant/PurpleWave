package Micro.Formation

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

class Formation(style: FormationStyle, val placements: Map[UnitInfo, Pixel]) {

  private lazy val default = if (placements.isEmpty) With.geography.home.center else Maff.exemplar(placements.values)

  def apply(unit: UnitInfo): Pixel = placements.getOrElse(unit, default)

  def renderMap(color: Color): Unit = {
    if (placements.nonEmpty) {
      DrawMap.label(style.name, Maff.centroid(placements.values), true, style.color)
    }
    placements.foreach(p => {
      val unit = p._1
      val slot = p._2
      val c = unit.unitClass
      DrawMap.line(p._1.pixel, slot, color)
      DrawMap.box(slot.subtract(c.dimensionLeft, c.dimensionUp), slot.add(c.dimensionRight, c.dimensionDown), color)
    })
  }
}
