package Planning.Predicates.Economy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class GasAtMost(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gas <= value
}
