package Geometry.Grids.Abstract

import Geometry.Grids.Real.GridItems
import Startup.With
import Types.UnitInfo.UnitInfo
import Utilities.Caching.Limiter
import bwapi.TilePosition

class GridUnits extends GridItems[UnitInfo] {
  
  override def _getTiles(unitInfo: UnitInfo):Iterable[TilePosition] = unitInfo.tileArea.tiles
  override def _getUnits: Iterable[UnitInfo] = With.units.all
  
  val _limitUpdates = new Limiter(1, () => super.update())
  override def update() = _limitUpdates.act()
}
