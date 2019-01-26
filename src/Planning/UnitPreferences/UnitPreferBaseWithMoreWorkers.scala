package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferBaseWithMoreWorkers extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = 1000 - UnitPreferBaseWithFewerWorkers.preference(unit)
}
