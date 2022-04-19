package Planning.Predicates.Strategy

import Planning.Predicates.{MacroFacts, Predicate}

case class StartPositionsAtLeast(count: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.starts >= count
}
