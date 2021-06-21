package Planning.Predicates.Strategy

import Planning.Predicate
import Planning.Predicates.MacroFacts

class StartPositionsAtMost(count: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.starts <= count
}