package Information.Grids.Vision

import ProxyBwapi.UnitInfo.UnitInfo
import Lifecycle.With

class GridFriendlyVision extends AbstractGridVision {
  
  override def units: Iterable[UnitInfo] = With.units.ours
}