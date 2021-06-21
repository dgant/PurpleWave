package Planning.Predicates.Strategy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyIsTerran() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsTerran
}