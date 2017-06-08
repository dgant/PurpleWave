package Information.Grids.Dps

import ProxyBwapi.UnitInfo.UnitInfo
import Lifecycle.With

abstract class AbstractGridDpsEnemy extends AbstractGridDps {
  override protected def getUnits: Iterable[UnitInfo] = With.units.enemy.filter(unit => unit.possiblyStillThere && unit.canAttackThisSecond)
}
