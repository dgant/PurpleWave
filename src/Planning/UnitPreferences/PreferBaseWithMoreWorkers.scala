package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferBaseWithMoreWorkers extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = 1000 - PreferBaseWithFewerWorkers.apply(unit)
}
