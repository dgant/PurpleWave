package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyWalledIn() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyWalledIn
}