package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferIdle extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.remainingOccupationFrames
}
