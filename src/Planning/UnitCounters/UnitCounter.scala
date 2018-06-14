package Planning.UnitCounters

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait UnitCounter {
  
  def reset() {}
  def continue  (units: Iterable[FriendlyUnitInfo]): Boolean
  def accept    (units: Iterable[FriendlyUnitInfo]): Boolean
  
}
