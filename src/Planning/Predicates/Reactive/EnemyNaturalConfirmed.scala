package Planning.Predicates.Reactive

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyNaturalConfirmed() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyNaturalConfirmed
}
