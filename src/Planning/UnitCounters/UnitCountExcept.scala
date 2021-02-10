package Planning.UnitCounters

import Lifecycle.With
import Planning.UnitMatchers.Matcher

class UnitCountExcept(buffer: Int, matcher: Matcher) extends UnitCountBetween(0, 0) {
  
  override def reset() {
    val cap = With.units.countOursP(unit => unit.friendly.exists(With.recruiter.eligible) && matcher.apply(unit))
    maximum.set(Math.max(0, cap - buffer))
  }
}