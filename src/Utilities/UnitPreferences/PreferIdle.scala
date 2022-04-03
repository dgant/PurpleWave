package Utilities.UnitPreferences

import Mathematics.Maff
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferIdle extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.remainingOccupationFrames + 240 * Maff.fromBoolean(unit.carrying)
}
