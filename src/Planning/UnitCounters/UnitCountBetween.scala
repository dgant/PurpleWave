package Planning.UnitCounters

import Planning.Property
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitCountBetween(originalMinimum: Int = 0, originalMaximum: Int = 1) extends UnitCounter {
  
  val minimum = new Property[Int](originalMinimum)
  val maximum = new Property[Int](originalMaximum)
  
  override def continue (units: Iterable[FriendlyUnitInfo]): Boolean = units.size < maximum.get
  override def accept   (units: Iterable[FriendlyUnitInfo]): Boolean = units.size >= minimum.get
}
