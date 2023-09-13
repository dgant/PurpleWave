package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class FoundEnemyBase() extends Predicate {
  override def apply: Boolean = MacroFacts.foundEnemyBase
}
