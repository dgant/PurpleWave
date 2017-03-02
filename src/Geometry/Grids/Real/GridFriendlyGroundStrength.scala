package Geometry.Grids.Real

import Geometry.Grids.Abstract.GridStrength
import Startup.With
import Types.UnitInfo.UnitInfo

class GridFriendlyGroundStrength extends GridStrength {
  override def _getUnits:Iterable[UnitInfo] = With.units.ours
}
