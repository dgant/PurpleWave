package Information.Grids.Concrete.Vision

import Information.Grids.Abstract.GridVision
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With

class GridEnemyVision extends GridVision {
  
  override def units: Iterable[UnitInfo] = With.units.enemy
}