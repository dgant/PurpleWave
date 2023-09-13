package Planning.Predicates.Economy

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class GasAtMost(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gas <= value
}
