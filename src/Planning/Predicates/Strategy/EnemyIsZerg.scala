package Planning.Predicates.Strategy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyIsZerg() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsZerg
}