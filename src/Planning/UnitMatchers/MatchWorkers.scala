package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchWorkers extends Matcher {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isWorker
}
