package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Preference {
  def apply(unit: FriendlyUnitInfo): Double
}
