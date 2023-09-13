package Planning.Predicates.Strategy

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyIsRandom() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsRandom
}