package Utilities.UnitFilters

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

object IsRangedGoon extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = Protoss.Dragoon(unit) && Protoss.DragoonRange(unit.player)
}
