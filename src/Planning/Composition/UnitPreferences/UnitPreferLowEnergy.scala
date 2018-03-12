package Planning.Composition.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferLowEnergy extends UnitPreference {
  
  override def preference(unit: FriendlyUnitInfo): Double = unit.energy
}
