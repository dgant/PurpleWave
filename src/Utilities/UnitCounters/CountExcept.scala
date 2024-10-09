package Utilities.UnitCounters

import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import Utilities.UnitFilters.UnitFilter

class CountExcept(buffer: Int, matcher: UnitFilter) extends UnitCounter {
  private val matched = new Cache(() => With.units.countOurs(matcher))
  override def minimum: Int = Maff.clamp01(matched() - buffer)
  override def maximum: Int = Math.max(0, matched() - buffer)
}