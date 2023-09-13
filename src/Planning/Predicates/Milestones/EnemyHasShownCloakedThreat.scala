package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyHasShownCloakedThreat() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyShownCloakedThreat
}