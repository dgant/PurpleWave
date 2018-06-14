package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchAnything, UnitMatchComplete, UnitMatcher}
import Planning.Predicate

class EnemiesAtLeast(
  quantity  : Int         = 0,
  matcher   : UnitMatcher = UnitMatchAnything,
  complete  : Boolean     = false)
  
  extends Predicate {
  
  override def isComplete: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countEnemy(UnitMatchAnd(UnitMatchComplete, matcher))
      }
      else {
        With.units.countEnemy(matcher)
      }
    val output = quantityFound >= quantity
    output
  }
}
