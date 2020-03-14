package Planning.UnitMatchers

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchInNatural extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.base.exists(With.scouting.enemyNatural.contains)
  
}
