package Planning.Predicates.Reactive

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyBasesAtMost(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.enemyBases <= value
}
