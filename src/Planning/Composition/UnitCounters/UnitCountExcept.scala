package Planning.Composition.UnitCounters

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatcher

class UnitCountExcept(buffer: Int, matcher: UnitMatcher) extends UnitCountBetween(0, 0) {
  
  override def reset() {
    val cap = With.units.ours.count(unit => With.recruiter.eligible(unit) && matcher.accept(unit))
    maximum.set(Math.max(0, cap - buffer))
  }
}