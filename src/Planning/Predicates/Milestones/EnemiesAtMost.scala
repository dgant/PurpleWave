package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate
import Utilities.UnitFilters.{IsAnything, UnitFilter}

case class EnemiesAtMost(quantity: Int = 0, matcher: UnitFilter = IsAnything, complete: Boolean = false) extends Predicate {
  override def apply: Boolean = {
    (if (complete) MacroFacts.enemiesComplete(matcher) else MacroFacts.enemies(matcher)) <= quantity
  }
}