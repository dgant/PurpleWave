package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchCombat(enemies: Iterable[UnitInfo]) extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean = {
    UnitMatchWarriors.accept(unit) && (
      ! unit.unitClass.canAttack ||
      enemies.exists(unit.canAttackThisSecond)
    )
  }
}
