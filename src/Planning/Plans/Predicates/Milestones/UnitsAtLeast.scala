package Planning.Plans.Predicates.Milestones

import Planning.Plan
import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}

class UnitsAtLeast(
  quantity  : Int         = 0,
  matcher   : UnitMatcher = UnitMatchAnything,
  complete  : Boolean     = false)
  
  extends Plan {
  
  description.set("Have at least " + quantity + " " + matcher)
  
  override def isComplete: Boolean = With.units.countOurs(unit =>
    ( ! complete || unit.complete) &&
    matcher.accept(unit)) >= quantity
}
