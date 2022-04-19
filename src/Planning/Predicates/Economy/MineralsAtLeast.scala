package Planning.Predicates.Economy

import Planning.Predicates.{MacroFacts, Predicate}

case class MineralsAtLeast(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.minerals >= value
}
