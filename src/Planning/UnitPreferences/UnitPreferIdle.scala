package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferIdle extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = unit.remainingOccupationFrames
}
