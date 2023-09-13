package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate
import Utilities.UnitFilters.{IsAnything, UnitFilter}

case class UnitsExactly(quantity: Int = 0, matcher: UnitFilter = IsAnything, complete: Boolean = false) extends Predicate {
  override def apply: Boolean = {
    (if (complete) MacroFacts.unitsComplete(matcher) else MacroFacts.units(matcher)) == quantity
  }
}