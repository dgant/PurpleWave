package Information.Grids.Dps

import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With

abstract class AbstractGridDpsEnemy extends AbstractGridDps {
  override protected def getUnits: Iterable[UnitInfo] = With.units.enemy.filter(_.canAttackThisSecond)
}
