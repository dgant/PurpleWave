package Planning.UnitPreferences

import Lifecycle.With
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferHatcheryWithThreeLarva extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double =
    Math.max(2, With.units
      .inTileRectangle(unit.tileIncludingCenter.toRectangle.expand(3, 3))
      .count(u => u != unit && u.isFriendly && u.is(Zerg.Larva)))
}
