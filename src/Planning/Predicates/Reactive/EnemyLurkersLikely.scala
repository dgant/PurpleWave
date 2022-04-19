package Planning.Predicates.Reactive

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyLurkersLikely() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyLurkersLikely
}
