package Planning.Plans.Predicates.Milestones

import Planning.Plan
import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchAnything, UnitMatchComplete, UnitMatcher}

class UnitsAtLeast(
  quantity  : Int,
  matcher   : UnitMatcher = UnitMatchAnything,
  complete  : Boolean     = false)
  
  extends Plan {
  
  description.set("Have at least " + quantity + " " + matcher)
  
  override def isComplete: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countOurs(UnitMatchAnd(UnitMatchComplete, matcher))
      }
      else {
        With.units.countOurs(matcher)
      }
    val output = quantityFound >= quantity
    output
  }
}
