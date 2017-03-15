package Information.Geography

import Geometry.TileRectangle
import Startup.With
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.{Player, Position, TilePosition}
import bwta.{Chokepoint, Region}
import Utilities.TypeEnrichment.EnrichPosition._

import scala.collection.mutable.ListBuffer

class Zone(
  val centroid:TilePosition,
  val region:Region,
  val bases:ListBuffer[Base],
  val edges:ListBuffer[ZoneEdge],
  var owner:Player = With.game.neutral)

class ZoneEdge(
  val chokepoint: Chokepoint,
  val zones:Iterable[Zone])

class Base(
  val zone:Zone,
  val townHallArea:TileRectangle,
  val harvestingArea:TileRectangle,
  val isStartLocation:Boolean)
{
  var townHall:Option[UnitInfo] = None
  def tile:TilePosition = townHallArea.startInclusive
  def centerTile:TilePosition = townHallArea.midpoint
  def centerPixel:Position = townHallArea.midpoint.centerPixel
}