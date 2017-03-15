package Information.Grids.Concrete

import Information.Grids.Abstract.GridStrength
import Startup.With
import BWMirrorProxy.UnitInfo.UnitInfo

class GridEnemyGroundStrength extends GridStrength {
  override def _getUnits: Iterable[UnitInfo] = With.units.enemy.filter(_.possiblyStillThere)
}
