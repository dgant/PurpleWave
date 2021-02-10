package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchWarriors extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = (
    unit.aliveAndComplete
    && unit.unitClass.dealsDamage
    && unit.unitClass.orderable
    && (unit.unitClass.canMove || unit.is(MatchSiegeTank))
    && ! unit.unitClass.isWorker
  )
}
