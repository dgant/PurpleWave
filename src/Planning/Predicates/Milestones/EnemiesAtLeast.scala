package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts
import Utilities.UnitMatchers.{MatchAnything, UnitMatcher}

case class EnemiesAtLeast(quantity: Int = 0, matcher: UnitMatcher = MatchAnything, complete: Boolean = false) extends Predicate {
  override def apply: Boolean = {
    (if (complete) MacroFacts.enemiesComplete(matcher) else MacroFacts.enemies(matcher)) >= quantity
  }
}
