package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchMobileDetector extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isDetector && unit.canMove
}
