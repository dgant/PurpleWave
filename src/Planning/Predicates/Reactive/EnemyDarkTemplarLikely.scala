package Planning.Predicates.Reactive

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyDarkTemplarLikely() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyDarkTemplarLikely
}
