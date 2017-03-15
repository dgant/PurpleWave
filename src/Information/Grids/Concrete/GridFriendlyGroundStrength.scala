package Information.Grids.Concrete

import Information.Grids.Abstract.GridStrength
import Startup.With
import BWMirrorProxy.UnitInfo.UnitInfo

class GridFriendlyGroundStrength extends GridStrength {
  override def _getUnits:Iterable[UnitInfo] = With.units.ours
}
