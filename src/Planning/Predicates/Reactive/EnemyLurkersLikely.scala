package Planning.Predicates.Reactive

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyLurkersLikely() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyLurkersLikely
}
