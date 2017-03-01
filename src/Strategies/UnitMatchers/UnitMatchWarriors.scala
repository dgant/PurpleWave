package Strategies.UnitMatchers

import Types.UnitInfo.FriendlyUnitInfo

class UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo):Boolean = {
    unit.utype.canAttack && unit.utype.canMove && ! unit.utype.isWorker
  }
}
