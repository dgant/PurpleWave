package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAttacksAir extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean =
    unit.unitClass.attacksAir
}
