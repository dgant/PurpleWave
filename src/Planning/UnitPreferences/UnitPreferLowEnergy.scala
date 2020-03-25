package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitPreferLowEnergy extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.energy
}
