package Planning.Predicates.Strategy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyIsProtoss() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsProtoss
}