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
      val box = Seq(
        slot.add( - c.dimensionLeft,  - c.dimensionUp),
        slot.add(   c.dimensionRight, - c.dimensionUp),
        slot.add(   c.dimensionRight,   c.dimensionDown),
        slot.add( - c.dimensionLeft,    c.dimensionDown)).map(_.subtract(style.offset, style.offset))

      if (p._1.pixelDistanceCenter(slot) < 320) {
        DrawMap.arrow(p._1.pixel, box.minBy(_.pixelDistanceSquared(p._1.pixel)), style.color)
      }
      if (unit.flying) {
        DrawMap.triangle(box(0).midpoint(box(1)), box(2), box(3), style.color)
      } else {
        DrawMap.box(box(0), box(2), style.color)
      }
    })
    DrawMap.polygon(Maff.convexHull(placements.flatMap(p => p._1.unitClass.corners.map(p._2.add)).toSeq), style.color)
  }
}