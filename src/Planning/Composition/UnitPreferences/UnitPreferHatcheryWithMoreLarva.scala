package Planning.Composition.UnitPreferences

import Lifecycle.With
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferHatcheryWithMoreLarva extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double =
    With.units
      .inRectangle(unit.tileIncludingCenter.toRectangle.expand(3, 3))
      .count(u => u != unit && u.isFriendly && u.is(Zerg.Larva))
}
