package Planning.Predicates.Reactive

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyDarkTemplarLikely() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyDarkTemplarLikely
}
