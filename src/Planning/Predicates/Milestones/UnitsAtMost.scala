package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.UnitMatchers.{MatchAnd, MatchAnything, MatchComplete, UnitMatcher}
import Planning.Predicate
import ProxyBwapi.Races.Zerg

class UnitsAtMost(
                   quantity:   Int,
                   matcher:    UnitMatcher = MatchAnything,
                   complete:   Boolean     = false)
  
  extends Predicate {
  
  override def apply: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countOurs(MatchAnd(MatchComplete, matcher))
      }
      else {
        With.units.ours.count(u => u.is(matcher) || (u.isAny(Zerg.Egg, Zerg.LurkerEgg) && u.buildType == matcher))
      }
    val output = quantityFound <= quantity
    output
  }
}
