package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyWalledIn() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyWalledIn
}