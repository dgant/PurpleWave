package Geometry.Grids.Abstract

import Geometry.Grids.Real.GridItems
import Startup.With
import Types.UnitInfo.UnitInfo
import bwapi.TilePosition

class GridUnits extends GridItems[UnitInfo] {
  
  override def _getTile(unitInfo: UnitInfo):TilePosition = unitInfo.tileCenter
  override def _getUnits: Iterable[UnitInfo] = With.units.all
}
