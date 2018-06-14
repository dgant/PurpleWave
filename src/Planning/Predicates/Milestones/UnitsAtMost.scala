package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchAnything, UnitMatchComplete, UnitMatcher}
import Planning.Predicate

class UnitsAtMost(
  quantity:   Int,
  matcher:    UnitMatcher = UnitMatchAnything,
  complete:   Boolean     = false)
  
  extends Predicate {
  
  override def isComplete: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countOurs(UnitMatchAnd(UnitMatchComplete, matcher))
      }
      else {
        With.units.countOurs(matcher)
      }
    val output = quantityFound <= quantity
    output
  }
}
