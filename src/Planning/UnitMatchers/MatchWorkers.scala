package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchWorkers extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isWorker
}
