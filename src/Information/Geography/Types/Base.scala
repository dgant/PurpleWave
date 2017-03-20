package Information.Geography.Types

import Geometry.TileRectangle
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.{Position, TilePosition}
import Utilities.TypeEnrichment.EnrichPosition._

class Base(
  val zone:Zone,
  val townHallArea:TileRectangle,
  val harvestingArea:TileRectangle,
  val isStartLocation:Boolean)
{
  var townHall:Option[UnitInfo] = None
  def tile:TilePosition = townHallArea.startInclusive
  def centerTile:TilePosition = townHallArea.midpoint
  def centerPixel:Position = townHallArea.midpoint.pixelCenter
}
