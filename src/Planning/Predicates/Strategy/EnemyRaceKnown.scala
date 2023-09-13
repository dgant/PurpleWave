package Planning.Predicates.Strategy

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyRaceKnown() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyRaceKnown
}