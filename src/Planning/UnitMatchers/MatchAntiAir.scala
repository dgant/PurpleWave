package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchAntiAir extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.damageOnHitAir > 0
}
