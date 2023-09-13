package Planning.Predicates.Reactive

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyMutalisksLikely() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyMutalisksLikely
}
