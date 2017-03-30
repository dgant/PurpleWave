package Information.Grids.Abstract.Dps

import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With

abstract class GridDpsEnemy extends GridDps {
  override protected def getUnits: Iterable[UnitInfo] = With.units.enemy.filter(_.canAttackRightNow)
}
