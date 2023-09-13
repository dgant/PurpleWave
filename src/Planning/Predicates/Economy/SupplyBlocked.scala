package Planning.Predicates.Economy

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class SupplyBlocked() extends Predicate {
  override def apply: Boolean = MacroFacts.supplyBlocked
}
