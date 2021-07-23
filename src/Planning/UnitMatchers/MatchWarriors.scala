package Planning.UnitMatchers

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

object MatchWarriors extends UnitMatcher {

  override def apply(unit: UnitInfo): Boolean = (
    unit.aliveAndComplete
    && unit.unitClass.dealsDamage
    && unit.unitClass.orderable
    && (unit.unitClass.canMove || Terran.SiegeTankSieged(unit))
    && ! unit.unitClass.isWorker
  )
}
