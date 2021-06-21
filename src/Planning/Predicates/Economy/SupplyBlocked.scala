package Planning.Predicates.Economy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class SupplyBlocked() extends Predicate {
  override def apply: Boolean = MacroFacts.supplyBlocked
}
