package Information.Geography.Types

import Geometry.TileRectangle
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import bwapi.{Position, TilePosition}
import Utilities.EnrichPosition._

class Base(
  val zone:Zone,
  val townHallArea:TileRectangle,
  val harvestingArea:TileRectangle,
  val isStartLocation:Boolean)
{
  var townHall:Option[UnitInfo] = None
  
  val mineralUnits:Set[UnitInfo] = With.units.neutral
    .filter(_.unitClass.isMinerals)
    .filter(mineral => zone.contains(mineral.pixelCenter))
    .toSet
  
  var minerals = 0
  var gas = 0
  
  def tile:TilePosition = townHallArea.startInclusive
  def centerTile:TilePosition = townHallArea.midpoint
  def centerPixel:Position = townHallArea.midpoint.pixelCenter
}
