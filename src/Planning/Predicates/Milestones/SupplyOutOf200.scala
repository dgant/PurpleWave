package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class SupplyOutOf200(quantity: Int = 0) extends Predicate {
  override def apply: Boolean = MacroFacts.supplyUsed200 >= quantity
}
