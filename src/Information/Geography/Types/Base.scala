package Information.Geography.Types

import Geometry.TileRectangle
import ProxyBwapi.UnitInfo.UnitInfo

class Base(
  val zone:Zone,
  val townHallRectangle:TileRectangle,
  val isStartLocation:Boolean)
{
  var townHall:Option[UnitInfo] = None
  var harvestingArea = townHallRectangle
  
  var gas       : Set[UnitInfo] = Set.empty
  var minerals  : Set[UnitInfo] = Set.empty
  
  var mineralsLeft  = 0
  var gasLeft       = 0
  var lastScouted   = 0
}
