package Planning.Predicates.Strategy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyIsRandom() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsRandom
}