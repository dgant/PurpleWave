package Planning.Composition.UnitMatchers

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchInNatural extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.base.exists(With.intelligence.enemyNatural.contains)
  
}
