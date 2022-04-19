package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}
import Utilities.UnitFilters.{IsAnything, UnitFilter}

case class UnitsAtMost(quantity: Int, matcher: UnitFilter = IsAnything, complete: Boolean = false) extends Predicate {
  override def apply: Boolean = {
    (if (complete) MacroFacts.unitsComplete(matcher) else MacroFacts.units(matcher)) <= quantity
  }
}
