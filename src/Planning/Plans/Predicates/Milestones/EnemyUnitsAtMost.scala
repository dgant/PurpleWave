package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Planning.Plan

class EnemyUnitsAtMost(
  quantity  : Int          = 0,
  matcher   : UnitMatcher  = UnitMatchAnything,
  complete  : Boolean       = false)
  
  extends Plan {
  
  description.set("Enemy has at most " + quantity + " " + matcher)
  
  override def isComplete: Boolean = With.units.countEnemy(unit =>
    ( ! complete || unit.complete) &&
    matcher.accept(unit)) <= quantity
}
