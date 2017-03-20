package Information.Grids.Concrete

import Information.Grids.Abstract.GridItems
import Performance.Caching.Limiter
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import bwapi.TilePosition

class GridUnits extends GridItems[UnitInfo] {
  
  override protected def getTiles(unitInfo: UnitInfo):Iterable[TilePosition] = unitInfo.tileArea.tiles
  override protected def getUnits: Iterable[UnitInfo] = With.units.all
  
  val _limitUpdates = new Limiter(1, () => super.update())
  override def update() = _limitUpdates.act()
}
