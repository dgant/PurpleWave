package Strategies.UnitMatchers

import Types.UnitInfo.FriendlyUnitInfo

object UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo):Boolean = {
    unit.complete && unit.utype.canAttack && unit.utype.canMove && ! unit.utype.isWorker
  }
}
