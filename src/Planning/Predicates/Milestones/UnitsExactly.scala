package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.UnitMatchers.{MatchAnd, MatchAnything, MatchComplete, Matcher}
import Planning.Predicate

class UnitsExactly(
                    quantity:   Int         = 0,
                    matcher:    Matcher = MatchAnything,
                    complete:   Boolean     = false)
  
  extends Predicate {
  
  override def isComplete: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countOurs(MatchAnd(MatchComplete, matcher))
      }
      else {
        With.units.countOurs(matcher)
      }
    val output = quantityFound == quantity
    output
  }
}
