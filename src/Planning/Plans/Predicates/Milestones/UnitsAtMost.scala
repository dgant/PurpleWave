package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchAnything, UnitMatchComplete, UnitMatcher}
import Planning.Plan

class UnitsAtMost(
  quantity:   Int,
  matcher:    UnitMatcher = UnitMatchAnything,
  complete:   Boolean     = false)
  
  extends Plan {
  
  description.set("Have at most " + quantity + " " + matcher)
  
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
