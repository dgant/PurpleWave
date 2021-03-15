package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.UnitMatchers.{MatchAnd, MatchAnything, MatchComplete, UnitMatcher}
import Planning.Predicate

class EnemiesAtLeast(
  quantity  : Int         = 0,
  matcher   : UnitMatcher = MatchAnything,
  complete  : Boolean     = false)
  
  extends Predicate {
  
  override def apply: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countEnemy(MatchAnd(MatchComplete, matcher))
      } else {
        With.units.countEnemy(matcher)
      }
    quantityFound >= quantity
  }
}
