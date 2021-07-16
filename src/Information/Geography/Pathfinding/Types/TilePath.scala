package Information.Geography.Pathfinding.Types

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.{Pixel, Tile}
import bwapi.Color

case class TilePath(
  start     : Tile,
  end       : Tile,
  distance  : Double,
  tiles     : Option[IndexedSeq[Tile]]) {
  
  def pathExists: Boolean = tiles.isDefined

  def renderMap(color: Color, from: Option[Pixel] = None): Unit = {
    val offset = from.map(_.offsetFromTileCenter).getOrElse(Pixel(0, 0))
      for (i <- 0 until tiles.get.size - 1) {
        DrawMap.arrow(
          tiles.get(i).center.add(offset),
          tiles.get(i + 1).center.add(offset),
          color)
      }
    }
}