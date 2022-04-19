package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class FoundEnemyBase() extends Predicate {
  override def apply: Boolean = MacroFacts.foundEnemyBase
}
