package Planning.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferAnything extends UnitPreference {
  override def preference(unit: FriendlyUnitInfo): Double = 0
}
