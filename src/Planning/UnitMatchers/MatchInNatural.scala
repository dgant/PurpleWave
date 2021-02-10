package Planning.UnitMatchers

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object MatchInNatural extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.base.exists(With.scouting.enemyNatural.contains)
  
}
