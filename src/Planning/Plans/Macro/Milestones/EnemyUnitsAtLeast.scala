package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Planning.Plan

class EnemyUnitsAtLeast(
  quantity  : Int         = 0,
  matcher   : UnitMatcher = UnitMatchAnything,
  complete  : Boolean     = false)
  
  extends Plan {
  
  description.set("Enemy has at least " + quantity + " matching units")
  
  override def isComplete: Boolean = With.units.enemy.count(unit =>
    ( ! complete || unit.complete) &&
    matcher.accept(unit)) >= quantity
}
