package Planning.UnitPreferences

import Lifecycle.With
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferHatcheryWithThreeLarva extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double =
    Math.max(2, With.units
      .inRectangle(unit.tileIncludingCenter.toRectangle.expand(3, 3))
      .count(u => u != unit && u.isFriendly && u.is(Zerg.Larva)))
}
