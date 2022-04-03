package Utilities.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferLowEnergy extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.energy
}
