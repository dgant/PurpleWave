package Information.Geography.Types

import Geometry.TileRectangle
import Startup.With
import bwapi.{Player, Position}
import bwta.Region

import scala.collection.mutable.ListBuffer

class Zone(
  val bwtaRegion:Region,
  val boundary:TileRectangle,
  val bases:ListBuffer[Base],
  val edges:ListBuffer[ZoneEdge]) {
  
  private val polygon = bwtaRegion.getPolygon
  
  val centroid = bwtaRegion.getCenter
  
  def contains(pixel:Position):Boolean = boundary.contains(pixel.toTilePosition) && polygon.isInside(pixel)
  
  var owner:Player = With.game.neutral
  
}
