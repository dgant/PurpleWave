package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class MineralOnlyBase() extends Predicate {
  override def apply: Boolean = MacroFacts.mineralOnlyBase
}
