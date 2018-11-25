package Planning.UnitCounters

import Lifecycle.With
import Planning.UnitMatchers.UnitMatcher

class UnitCountExcept(buffer: Int, matcher: UnitMatcher) extends UnitCountBetween(0, 0) {
  
  override def reset() {
    val cap = With.units.countOursP(unit => unit.friendly.exists(With.recruiter.eligible) && matcher.accept(unit))
    maximum.set(Math.max(0, cap - buffer))
  }
}