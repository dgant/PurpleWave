package Information.Grids.Concrete

import Information.Grids.Abstract.GridItems
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import bwapi.TilePosition

class GridUnits extends GridItems[UnitInfo] {
  
  override protected def getTiles(unitInfo: UnitInfo):Iterable[TilePosition] = unitInfo.tileArea.tiles
  override protected def getUnits: Iterable[UnitInfo] = With.units.all
}
