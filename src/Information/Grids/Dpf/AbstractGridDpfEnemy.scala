package Information.Grids.Dpf

import ProxyBwapi.UnitInfo.UnitInfo
import Lifecycle.With

abstract class AbstractGridDpfEnemy extends AbstractGridDpf {
  override protected def getUnits: Iterable[UnitInfo] = With.units.enemy.filter(unit => unit.possiblyStillThere && unit.canAttackThisSecond)
}
