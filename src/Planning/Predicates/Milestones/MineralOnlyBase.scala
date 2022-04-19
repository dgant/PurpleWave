package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class MineralOnlyBase() extends Predicate {
  override def apply: Boolean = MacroFacts.mineralOnlyBase
}
