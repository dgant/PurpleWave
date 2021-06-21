package Planning.Predicates.Reactive

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyMutalisksLikely() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyMutalisksLikely
}
