package Planning.Predicates.Economy

import Planning.Predicates.{MacroFacts, Predicate}

case class GasAtMost(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gas <= value
}
