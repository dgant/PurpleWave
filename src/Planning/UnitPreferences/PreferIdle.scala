package Planning.UnitPreferences

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferIdle extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.remainingOccupationFrames + 240 * PurpleMath.fromBoolean(unit.carrying)
}
