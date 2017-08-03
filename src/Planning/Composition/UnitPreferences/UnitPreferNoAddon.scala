package Planning.Composition.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferNoAddon extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = unit.addon.size
}
