package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyHasShownCloakedThreat() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyShownCloakedThreat
}