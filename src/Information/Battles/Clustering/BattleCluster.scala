package Information.Battles.Clustering

import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleCluster {
  var units: mutable.HashSet[UnitInfo] = new mutable.HashSet[UnitInfo]
  def merge(other: BattleCluster): Unit = {
    // Merge one set into the other
    // Prefer to use the already-larger set to minimize number of insertions
    val useOurUnits = units.size > other.units.size
    val newUnits = if (useOurUnits) units else other.units
    val oldUnits = if (useOurUnits) other.units else units
    newUnits ++= oldUnits
    units = newUnits
    other.units = newUnits
  }
  override def equals(other: scala.Any): Boolean = other.isInstanceOf[BattleCluster] && other.asInstanceOf[BattleCluster].units.eq(units)
}
