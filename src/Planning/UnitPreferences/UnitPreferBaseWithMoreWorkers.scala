package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferBaseWithMoreWorkers extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = 1000 - UnitPreferBaseWithFewerWorkers.apply(unit)
}
