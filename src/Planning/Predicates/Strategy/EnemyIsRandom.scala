package Planning.Predicates.Strategy

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyIsRandom() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsRandom
}