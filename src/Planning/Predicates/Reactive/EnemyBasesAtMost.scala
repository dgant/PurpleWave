package Planning.Predicates.Reactive

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyBasesAtMost(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.enemyBases <= value
}
