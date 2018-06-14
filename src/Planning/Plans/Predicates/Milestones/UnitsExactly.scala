package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchAnything, UnitMatchComplete, UnitMatcher}
import Planning.Predicate

class UnitsExactly(
  quantity:   Int         = 0,
  matcher:    UnitMatcher = UnitMatchAnything,
  complete:   Boolean     = false)
  
  extends Predicate {
  
  description.set("Have exactly " + quantity + " " + matcher)
  
  override def isComplete: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countOurs(UnitMatchAnd(UnitMatchComplete, matcher))
      }
      else {
        With.units.countOurs(matcher)
      }
    val output = quantityFound == quantity
    output
  }
}
