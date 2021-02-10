package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchAntiGround extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.damageOnHitGround > 0
}
