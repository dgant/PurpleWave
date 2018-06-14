package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAntiGround extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.damageOnHitGround > 0
}
