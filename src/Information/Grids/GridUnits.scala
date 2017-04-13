package Information.Grids

import Information.Grids.ArrayTypes.AbstractGridVector
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.TilePosition

class GridUnits extends AbstractGridVector[UnitInfo] {
  
  override protected def getObjects: Iterable[UnitInfo] = With.units.all
  override protected def getTile(item: UnitInfo): TilePosition = item.tileIncludingCenter
}
