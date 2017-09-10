package Planning.Composition.UnitCounters

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatcher

class UnitCountExcept(buffer: Int, matcher: UnitMatcher) extends UnitCountBetween(0, 0) {
  
  override def reset() { maximum.set(Math.max(0, With.units.ours.count(With.recruiter.eligible) - buffer))}
}