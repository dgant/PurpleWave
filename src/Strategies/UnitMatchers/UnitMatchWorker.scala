package Strategies.UnitMatchers

import Types.UnitInfo.FriendlyUnitInfo

object UnitMatchWorker extends UnitMatcher{
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    return unit.unitType.isWorker
  }
}
