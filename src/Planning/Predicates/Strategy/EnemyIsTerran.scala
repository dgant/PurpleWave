package Planning.Predicates.Strategy

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyIsTerran() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsTerran
}