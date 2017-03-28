package Information.Geography.Types

import Geometry.TileRectangle
import Startup.With
import bwapi.{Player, Position, TilePosition}
import bwta.Region

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Zone(
  val bwtaRegion:Region,
  val groundHeight:Int,
  val boundary:TileRectangle,
  val tiles:mutable.Set[TilePosition],
  val bases:ListBuffer[Base],
  val edges:ListBuffer[ZoneEdge]) {
  
  val centroid = bwtaRegion.getCenter
  var owner:Player = With.game.neutral
  val points:Iterable[Position] = bwtaRegion.getPolygon.getPoints.asScala.toList
  val island:Boolean = ! With.game.getStartLocations.asScala.exists(startTile => With.paths.exists(centroid.toTilePosition, startTile))
  
  def contains(tile:TilePosition):Boolean = boundary.contains(tile) && tiles.contains(tile)
  def contains(pixel:Position):Boolean = contains(pixel.toTilePosition)
}
