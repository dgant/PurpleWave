package Planning.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferAnything extends UnitPreference {
  override def apply(unit: FriendlyUnitInfo): Double = 0
}
