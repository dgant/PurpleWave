package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.UnitMatchers._
import Planning.Predicate
import ProxyBwapi.Races.Zerg

class UnitsAtLeast(
                    quantity  : Int,
                    matcher   : Matcher,
                    complete  : Boolean     = false,
                    countEggs : Boolean     = false) // TMP: Resolve after AIST1
  
  extends Predicate {
  
  override def isComplete: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countOurs(MatchAnd(MatchComplete, matcher))
      }
      else if (countEggs) {
        With.units.ours.count(u => u.is(matcher) || (u.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon) && u.buildType == matcher))
      }
      else {
        With.units.countOurs(matcher)
      }
    val output = quantityFound >= quantity
    output
  }
}
