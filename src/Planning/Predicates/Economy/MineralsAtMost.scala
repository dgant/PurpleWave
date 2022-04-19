package Planning.Predicates.Economy

import Planning.Predicates.{MacroFacts, Predicate}

case class MineralsAtMost(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.minerals <= value
}
