package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo):Boolean = {
    unit.complete &&
      unit.impactsCombat &&
      unit.utype.canMove &&
      unit.utype.orderable &&
      ! unit.utype.isWorker
  }
}
