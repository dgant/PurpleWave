package Information.Grids.Concrete

import Information.Grids.Abstract.GridStrength
import Startup.With
import ProxyBwapi.UnitInfo.UnitInfo

class GridFriendlyGroundStrength extends GridStrength {
  override protected  def getUnits:Iterable[UnitInfo] = With.units.ours
}
