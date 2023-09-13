package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate
import Utilities.UnitFilters._

case class UnitsAtLeast(quantity: Int, matcher: UnitFilter, complete: Boolean = false) extends Predicate {
  override def apply: Boolean = {
    (if (complete) MacroFacts.unitsComplete(matcher) else MacroFacts.units(matcher)) >= quantity
  }
}
