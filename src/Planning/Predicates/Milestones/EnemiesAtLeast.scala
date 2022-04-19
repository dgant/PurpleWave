package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}
import Utilities.UnitFilters.{IsAnything, UnitFilter}

case class EnemiesAtLeast(quantity: Int = 0, matcher: UnitFilter = IsAnything, complete: Boolean = false) extends Predicate {
  override def apply: Boolean = {
    (if (complete) MacroFacts.enemiesComplete(matcher) else MacroFacts.enemies(matcher)) >= quantity
  }
}
