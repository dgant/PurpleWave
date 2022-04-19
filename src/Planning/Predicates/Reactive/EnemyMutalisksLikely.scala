package Planning.Predicates.Reactive

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyMutalisksLikely() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyMutalisksLikely
}
