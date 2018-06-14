package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait UnitPreference {
  def preference(unit: FriendlyUnitInfo): Double
}
