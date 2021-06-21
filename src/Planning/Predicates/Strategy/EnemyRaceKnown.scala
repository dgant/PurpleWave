package Planning.Predicates.Strategy

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyRaceKnown() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyRaceKnown
}