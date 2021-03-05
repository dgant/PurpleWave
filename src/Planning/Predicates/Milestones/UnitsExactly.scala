package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.UnitMatchers.{MatchAnd, MatchAnything, MatchComplete, UnitMatcher}
import Planning.Predicate

class UnitsExactly(
                    quantity:   Int         = 0,
                    matcher:    UnitMatcher = MatchAnything,
                    complete:   Boolean     = false)
  
  extends Predicate {
  
  override def apply: Boolean = {
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
