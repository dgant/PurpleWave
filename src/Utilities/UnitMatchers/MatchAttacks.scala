package Utilities.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchAttacks extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean =
    unit.canAttack
}
