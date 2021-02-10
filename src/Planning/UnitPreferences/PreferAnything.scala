package Planning.UnitPreferences
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferAnything extends Preference {
  override def apply(unit: FriendlyUnitInfo): Double = 0
}
