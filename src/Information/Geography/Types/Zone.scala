package Information.Geography.Types

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import ProxyBwapi.Players.PlayerInfo
import bwta.Region

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Zone(
  val bwtaRegion  : Region,
  val altitude    : Int,
  val boundary    : TileRectangle,
  val tiles       : mutable.Set[Tile],
  val bases       : ListBuffer[Base],
  val edges       : ListBuffer[ZoneEdge]) {
  
  val centroid = new Pixel(bwtaRegion.getCenter)
  var owner:PlayerInfo = With.neutral
  val area:Double = bwtaRegion.getPolygon.getArea
  val points:Iterable[Pixel] = bwtaRegion.getPolygon.getPoints.asScala.map(new Pixel(_)).toVector
  val island:Boolean = ! With.game.getStartLocations.asScala.map(new Tile(_)).exists(startTile => With.paths.exists(centroid.tileIncluding, startTile))
  
  def contains(tile:Tile):Boolean = boundary.contains(tile) && tiles.contains(tile)
  def contains(pixel:Pixel):Boolean = contains(pixel.tileIncluding)
}
