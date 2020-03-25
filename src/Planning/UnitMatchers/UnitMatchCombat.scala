package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchCombat(enemies: Iterable[UnitInfo]) extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = {
    UnitMatchWarriors.apply(unit) && (
      ! unit.unitClass.rawCanAttack || enemies.exists(unit.canAttack)
    )
  }
}
