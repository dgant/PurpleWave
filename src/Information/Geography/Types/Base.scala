package Information.Geography.Types

import Mathematics.Points.{Tile, TileRectangle}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo

class Base(
  val zone            : Zone,
  val townHallArea    : TileRectangle,
  val isStartLocation : Boolean)
{
  var townHall        : Option[UnitInfo]  = None
  var harvestingArea  : TileRectangle     = townHallArea
  var heart           : Tile              = harvestingArea.midpoint
  var gas             : Set[UnitInfo]     = Set.empty
  var minerals        : Set[UnitInfo]     = Set.empty
  var workers         : Set[UnitInfo]     = Set.empty
  var walledIn        : Boolean           = false
  var planningToTake  : Boolean           = false
  
  var mineralsLeft      = 0
  var gasLeft           = 0
  var lastScoutedFrame  = 0
  
  def owner: PlayerInfo = zone.owner
  def resources: Set[UnitInfo] = minerals ++ gas
}
