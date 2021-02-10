package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.UnitMatchers.{MatchAnd, MatchAnything, MatchComplete, UnitMatcher}
import Planning.Predicate

class EnemiesAtMost(
                     quantity  : Int          = 0,
                     matcher   : UnitMatcher  = MatchAnything,
                     complete  : Boolean       = false)
  
  extends Predicate {
  
  override def isComplete: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countEnemy(MatchAnd(MatchComplete, matcher))
      }
      else {
        With.units.countEnemy(matcher)
      }
    val output = quantityFound <= quantity
    output
  }
}
