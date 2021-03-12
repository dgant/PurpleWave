package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchWorker extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isWorker
}
