package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAntiAir extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.damageOnHitAir > 0
}
