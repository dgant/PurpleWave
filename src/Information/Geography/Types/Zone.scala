package Information.Geography.Types

import Lifecycle.With
import Mathematics.Positions.TileRectangle
import bwapi.{Player, Position, TilePosition}
import bwta.Region

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import Utilities.EnrichPosition._

class Zone(
  val bwtaRegion:Region,
  val groundHeight:Int,
  val boundary:TileRectangle,
  val tiles:mutable.Set[TilePosition],
  val bases:ListBuffer[Base],
  val edges:ListBuffer[ZoneEdge]) {
  
  val centroid = bwtaRegion.getCenter
  var owner:Player = With.neutral
  val area:Double = bwtaRegion.getPolygon.getArea
  val points:Iterable[Position] = bwtaRegion.getPolygon.getPoints.asScala.toList
  val island:Boolean = ! With.game.getStartLocations.asScala.exists(startTile => With.paths.exists(centroid.tileIncluding, startTile))
  
  def contains(tile:TilePosition):Boolean = boundary.contains(tile) && tiles.contains(tile)
  def contains(pixel:Position):Boolean = contains(pixel.tileIncluding)
}
