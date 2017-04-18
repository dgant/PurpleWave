package Information.Geography.Types

import Mathematics.Pixels.{Tile, TileRectangle}
import ProxyBwapi.UnitInfo.UnitInfo

class Base(
  val zone:Zone,
  val townHallArea:TileRectangle,
  val isStartLocation:Boolean)
{
  var townHall:Option[UnitInfo] = None
  var harvestingArea = townHallArea
  
  var gas       : Set[UnitInfo] = Set.empty
  var minerals  : Set[UnitInfo] = Set.empty
  
  var mineralsLeft      = 0
  var gasLeft           = 0
  var lastScoutedFrame  = 0
  
  var heart:Tile = harvestingArea.midpoint
}
