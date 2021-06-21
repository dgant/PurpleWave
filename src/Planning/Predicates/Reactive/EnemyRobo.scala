package Planning.Predicates.Reactive

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyRobo() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyRobo
}