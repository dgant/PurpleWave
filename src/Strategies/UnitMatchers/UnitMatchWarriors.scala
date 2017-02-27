package Strategies.UnitMatchers

import Types.UnitInfo.FriendlyUnitInfo

class UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo):Boolean = {
    unit.unitType.canAttack && unit.unitType.canMove && ! unit.unitType.isWorker
  }
}
