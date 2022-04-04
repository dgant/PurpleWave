package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts
import Utilities.UnitFilters._

case class UnitsAtLeast(quantity: Int, matcher: UnitFilter, complete: Boolean = false) extends Predicate {
  override def apply: Boolean = {
    (if (complete) MacroFacts.unitsComplete(matcher) else MacroFacts.units(matcher)) >= quantity
  }
}
