package Planning.Predicates.Economy

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class MineralsAtMost(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.minerals <= value
}
