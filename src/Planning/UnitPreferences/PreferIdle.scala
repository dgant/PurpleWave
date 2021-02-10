package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferIdle extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.remainingOccupationFrames
}
