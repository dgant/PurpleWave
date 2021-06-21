package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class FoundEnemyBase() extends Predicate {
  override def apply: Boolean = MacroFacts.foundEnemyBase
}
