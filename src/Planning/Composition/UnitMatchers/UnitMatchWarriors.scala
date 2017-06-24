package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean =
    unit.aliveAndComplete &&
    unit.unitClass.helpsInCombat &&
    unit.unitClass.canMove &&
    unit.unitClass.orderable &&
    ! unit.unitClass.isWorker
}
