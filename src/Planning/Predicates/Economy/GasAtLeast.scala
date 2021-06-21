package Planning.Predicates.Economy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class GasAtLeast(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gas >= value
}
