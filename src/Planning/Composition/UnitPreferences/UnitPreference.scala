package Planning.Composition.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait UnitPreference {
  def preference(unit:FriendlyUnitInfo):Double
}
