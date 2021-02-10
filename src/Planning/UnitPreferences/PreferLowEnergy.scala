package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferLowEnergy extends Preference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.energy
}
