package Planning.Predicates.Strategy

import Planning.MacroFacts
import Planning.Predicates.Predicate

class StartPositionsAtMost(count: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.starts <= count
}