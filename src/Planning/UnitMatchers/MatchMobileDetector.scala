package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchMobileDetector extends Matcher {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isDetector && unit.canMove
}
