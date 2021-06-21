package Planning.Predicates.Strategy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class StartPositionsAtLeast(count: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.starts >= count
}
