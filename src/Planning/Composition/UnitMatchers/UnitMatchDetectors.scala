package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchDetectors extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean = unit.unitClass.isDetector
}
