package Utilities.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferAnything extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = 0
}
