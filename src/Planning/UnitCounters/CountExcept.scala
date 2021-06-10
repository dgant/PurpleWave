package Planning.UnitCounters

import Lifecycle.With
import Mathematics.PurpleMath
import Performance.Cache
import Planning.UnitMatchers.UnitMatcher

class CountExcept(buffer: Int, matcher: UnitMatcher) extends UnitCounter {
  private val matched = new Cache(() => With.units.countOurs(matcher))
  override def minimum: Int = PurpleMath.clamp(matched() - buffer, 0, 1)
  override def maximum: Int = Math.max(0, matched() - buffer)
}