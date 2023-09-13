package Planning.Predicates.Strategy

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class StartPositionsAtLeast(count: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.starts >= count
}
