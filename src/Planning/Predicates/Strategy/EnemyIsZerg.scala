package Planning.Predicates.Strategy

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyIsZerg() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyIsZerg
}