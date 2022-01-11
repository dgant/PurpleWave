package Micro.Formation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Formation {
  def style: FormationStyle
  def placements: Map[FriendlyUnitInfo, Pixel]
  def renderMap(): Unit = {
    placements.foreach(p => {
      val unit = p._1
      val slot = p._2
      val c = unit.unitClass
      DrawMap.line(p._1.pixel, slot, style.color)
      DrawMap.box(
        slot
          .subtract(c.dimensionLeft, c.dimensionUp)
          .add(style.offset, style.offset),
        slot
          .add(c.dimensionRight, c.dimensionDown)
          .subtract(style.offset, style.offset),
        style.color)})
    DrawMap.polygon(Maff.convexHull(placements.flatMap(p => p._1.unitClass.corners.map(p._2.add)).toSeq), style.color)
  }
}