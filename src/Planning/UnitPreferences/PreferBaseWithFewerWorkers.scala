package Planning.UnitPreferences

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object PreferBaseWithFewerWorkers extends Preference {
  
  override def apply(unit: FriendlyUnitInfo): Double = unit.base.map(_.workerCount).getOrElse(0).toDouble
}
