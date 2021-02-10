package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferBaseWithFewerWorkers extends UnitPreference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.base.map(_.workerCount).getOrElse(0).toDouble
}
