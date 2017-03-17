package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo):Boolean = {
    unit.complete &&
      unit.impactsCombat &&
      unit.unitClass.canMove &&
      unit.unitClass.orderable &&
      ! unit.unitClass.isWorker
  }
}
