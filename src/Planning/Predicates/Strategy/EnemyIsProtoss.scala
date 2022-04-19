package Planning.Predicates.Strategy

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyIsProtoss() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsProtoss
}