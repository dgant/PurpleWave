package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchAttacks extends Matcher {
  override def apply(unit: UnitInfo): Boolean =
    unit.canAttack
}
