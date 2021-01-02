package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchWarriors extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = (
    unit.aliveAndComplete
    && unit.unitClass.dealsDamage
    && unit.unitClass.orderable
    && (unit.unitClass.canMove || unit.is(UnitMatchSiegeTank))
    && ! unit.unitClass.isWorker
  )
}
