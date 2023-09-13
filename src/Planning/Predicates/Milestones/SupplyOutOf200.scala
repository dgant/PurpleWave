package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class SupplyOutOf200(quantity: Int = 0) extends Predicate {
  override def apply: Boolean = MacroFacts.supplyUsed200 >= quantity
}
