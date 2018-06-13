package Planning.Composition.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferBaseWithFewerWorkers extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = unit.base.map(_.workers.size).sum
}