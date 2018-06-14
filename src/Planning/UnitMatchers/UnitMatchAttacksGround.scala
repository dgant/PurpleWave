package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAttacksGround extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean =
    unit.unitClass.attacksGround
}
