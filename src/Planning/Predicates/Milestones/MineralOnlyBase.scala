package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class MineralOnlyBase() extends Predicate {
  override def apply: Boolean = MacroFacts.mineralOnlyBase
}
