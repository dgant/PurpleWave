package Planning.Predicates.Economy

import Planning.Predicates.{MacroFacts, Predicate}

case class SupplyBlocked() extends Predicate {
  override def apply: Boolean = MacroFacts.supplyBlocked
}
