package Planning.Predicates.Reactive

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyRobo() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyRobo
}