package Planning.Predicates.Reactive

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyBasesAtLeast(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.enemyBases >= value
}
