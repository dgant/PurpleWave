package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferIdle extends Preference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.remainingOccupationFrames
}
