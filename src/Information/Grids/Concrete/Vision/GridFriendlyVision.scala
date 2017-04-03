package Information.Grids.Concrete.Vision

import Information.Grids.Abstract.GridVision
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With

class GridFriendlyVision extends GridVision {
  
  override def units: Iterable[UnitInfo] = With.units.ours
}