package Strategies.UnitMatchers

class UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: bwapi.Unit):Boolean = {
    unit.getType.canAttack && unit.getType.canMove && ! unit.getType.isWorker
  }
}
