package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyHasShownCloakedThreat() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyShownCloakedThreat
}