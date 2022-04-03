package Utilities.UnitMatchers

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

object MatchWarriors extends UnitMatcher {

  override def apply(unit: UnitInfo): Boolean = (
    unit.aliveAndComplete
    && unit.unitClass.orderable
    && unit.unitClass.attacksOrCasts
    && ! unit.unitClass.isWorker
    && (unit.unitClass.canMove || Terran.SiegeTankSieged(unit))
  )
}
