package Information.Grids.Abstract

import Information.Grids.Concrete.GridItems
import Startup.With
import ProxyBwapi.UnitInfo.UnitInfo
import Performance.Caching.Limiter
import bwapi.TilePosition

class GridUnits extends GridItems[UnitInfo] {
  
  override protected def getTiles(unitInfo: UnitInfo):Iterable[TilePosition] = unitInfo.tileArea.tiles
  override protected def getUnits: Iterable[UnitInfo] = With.units.all
  
  val _limitUpdates = new Limiter(1, () => super.update())
  override def update() = _limitUpdates.act()
}
