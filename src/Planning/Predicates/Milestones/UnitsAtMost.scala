package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts
import Utilities.UnitMatchers.{MatchAnything, UnitMatcher}

case class UnitsAtMost(quantity: Int, matcher: UnitMatcher = MatchAnything, complete: Boolean = false) extends Predicate {
  override def apply: Boolean = {
    (if (complete) MacroFacts.unitsComplete(matcher) else MacroFacts.units(matcher)) <= quantity
  }
}
