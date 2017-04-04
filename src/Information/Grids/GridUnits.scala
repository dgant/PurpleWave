package Information.Grids

import Information.Grids.ArrayTypes.AbstractGridSet
import ProxyBwapi.UnitInfo.UnitInfo
import Lifecycle.With
import bwapi.TilePosition

class GridUnits extends AbstractGridSet[UnitInfo] {
  
  override protected def getTiles(unitInfo: UnitInfo):Iterable[TilePosition] = unitInfo.tileArea.tiles
  override protected def getUnits: Iterable[UnitInfo] = With.units.all
}
