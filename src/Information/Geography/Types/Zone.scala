package Information.Geography.Types

import Geometry.TileRectangle
import Startup.With
import bwapi.{Player, Position, TilePosition}
import bwta.Region

import scala.collection.mutable.ListBuffer

class Zone(
  val bwtaRegion:Region,
  val boundary:TileRectangle,
  val tiles:Set[TilePosition],
  val bases:ListBuffer[Base],
  val edges:ListBuffer[ZoneEdge]) {
  
  private val polygon = bwtaRegion.getPolygon
  
  val centroid = bwtaRegion.getCenter
  var owner:Player = With.game.neutral
  
  def contains(pixel:Position):Boolean = boundary.contains(pixel.toTilePosition) && tiles.contains(pixel.toTilePosition)
}
