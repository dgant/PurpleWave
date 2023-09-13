package Planning.Predicates.Strategy

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyIsProtoss() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsProtoss
}