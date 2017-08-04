package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAttacks extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean =
    unit.canAttack
}
