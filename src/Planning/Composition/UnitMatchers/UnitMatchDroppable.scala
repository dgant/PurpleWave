package Planning.Composition.UnitMatchers

import Planning.Composition.UnitPreferences.UnitPreferDroppable
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchDroppable extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean =
    unit.aliveAndComplete &&
    UnitPreferDroppable.preferenceOrder.contains(unit.unitClass)
}
