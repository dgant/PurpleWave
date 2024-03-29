package Utilities.UnitPreferences

import Lifecycle.With
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferHatcheryWithThreeLarva extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double =
    Math.max(2, With.units
      .inTileRectangle(unit.tile.toRectangle.expand(3, 3))
      .count(u => u != unit && u.isFriendly && u.is(Zerg.Larva)))
}
