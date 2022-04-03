package Utilities.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchAntiAir extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.damageOnHitAir > 0
}
