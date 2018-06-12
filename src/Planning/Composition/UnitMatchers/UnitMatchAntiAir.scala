package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAntiAir extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.damageOnHitAir > 0
}
