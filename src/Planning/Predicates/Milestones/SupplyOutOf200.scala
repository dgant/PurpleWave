package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class SupplyOutOf200(quantity: Int = 0) extends Predicate {
  override def apply: Boolean = MacroFacts.supplyUsed200 >= quantity
}
