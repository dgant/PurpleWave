package Planning.Predicates.Reactive

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyDarkTemplarLikely() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyDarkTemplarLikely
}
