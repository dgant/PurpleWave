package Planning.Predicates.Strategy

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyIsZerg() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsZerg
}