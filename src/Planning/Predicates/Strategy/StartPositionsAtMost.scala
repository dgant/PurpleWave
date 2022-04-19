package Planning.Predicates.Strategy

import Planning.Predicates.{MacroFacts, Predicate}

class StartPositionsAtMost(count: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.starts <= count
}