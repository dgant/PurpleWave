package Planning.Predicates.Economy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class MineralsAtMost(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.minerals <= value
}
