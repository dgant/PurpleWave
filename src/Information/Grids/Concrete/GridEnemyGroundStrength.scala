package Information.Grids.Concrete

import Information.Grids.Abstract.GridStrength
import Startup.With
import ProxyBwapi.UnitInfo.UnitInfo

class GridEnemyGroundStrength extends GridStrength {
  override protected  def getUnits: Iterable[UnitInfo] = With.units.enemy.filter(_.possiblyStillThere)
}
