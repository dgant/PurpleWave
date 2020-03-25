package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAttacks extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean =
    unit.canAttack
}
