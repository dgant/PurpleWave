package Information.Grids

import Information.Grids.ArrayTypes.AbstractGridVector
import Lifecycle.With
import Mathematics.Pixels.Tile
import ProxyBwapi.UnitInfo.UnitInfo

class GridUnits extends AbstractGridVector[UnitInfo] {
  
  override protected def getObjects: Traversable[UnitInfo] = With.units.all
  override protected def getTile(item: UnitInfo): Tile = item.tileIncludingCenter
}
