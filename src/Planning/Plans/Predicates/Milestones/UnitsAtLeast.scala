package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Composition.UnitMatchers._
import Planning.Predicate
import ProxyBwapi.Races.Zerg

class UnitsAtLeast(
  quantity  : Int,
  matcher   : UnitMatcher = UnitMatchAnything,
  complete  : Boolean     = false,
  countEggs : Boolean     = false) // TMP: Resolve after AIST1
  
  extends Predicate {
  
  description.set("Have at least " + quantity + " " + matcher)
  
  override def isComplete: Boolean = {
    val quantityFound =
      if (complete) {
        With.units.countOurs(UnitMatchAnd(UnitMatchComplete, matcher))
      }
      else if (countEggs) {
        With.units.ours.count(u => u.is(matcher) || (u.isAny(Zerg.Egg, Zerg.LurkerEgg) && u.buildType == matcher))
      }
      else {
        With.units.countOurs(matcher)
      }
    val output = quantityFound >= quantity
    output
  }
}
