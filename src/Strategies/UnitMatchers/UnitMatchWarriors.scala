package Strategies.UnitMatchers

class UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: bwapi.Unit):Boolean = {
    unit.canAttack && unit.canMove && ! unit.canBuild
  }
}
