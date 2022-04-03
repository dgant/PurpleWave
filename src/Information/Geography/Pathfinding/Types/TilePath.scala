package Information.Geography.Pathfinding.Types

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.{Pixel, Point, SpecificPoints, Tile}
import bwapi.Color

case class TilePath(
  start     : Tile,
  end       : Tile,
  distance  : Double,
  tiles     : Option[IndexedSeq[Tile]]) {

  def this() {
    this(SpecificPoints.tileMiddle, SpecificPoints.tileMiddle, 0, None)
  }
  
  def pathExists: Boolean = tiles.isDefined

  def length: Int = tiles.map(_.length).getOrElse(0)

  def renderMap(color: Color, from: Option[Pixel] = None, customOffset: Point = Point(0, 0)): Unit = {
    if (tiles.isEmpty) return
    val offset = from.map(_.offsetFromTileCenter).getOrElse(Pixel(0, 0)).add(customOffset)
    for (i <- 0 until tiles.get.size - 1) {
      DrawMap.arrow(
        tiles.get(i).center.add(offset),
        tiles.get(i + 1).center.add(offset),
        color)
    }
  }
}