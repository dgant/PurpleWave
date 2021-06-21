package Planning.Predicates.Reactive

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyLurkersLikely() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyLurkersLikely
}
