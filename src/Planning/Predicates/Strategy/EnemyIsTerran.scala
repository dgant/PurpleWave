package Planning.Predicates.Strategy

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyIsTerran() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsTerran
}