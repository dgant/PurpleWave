package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

case class MatchCombat(enemies: Iterable[UnitInfo]) extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = {
    MatchWarriors.apply(unit) && (
      ! unit.unitClass.rawCanAttack || enemies.exists(unit.canAttack)
    )
  }
}
