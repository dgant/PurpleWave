package Planning.Predicates.Strategy

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyRaceKnown() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyRaceKnown
}