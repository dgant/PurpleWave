package Strategies.UnitCounters

import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property

class UnitCountExactly(var originalQuantity:Int = 1) extends UnitCounter {
  
  val quantity = new Property[Int](originalQuantity)
  
  override def continue(units: Iterable[FriendlyUnitInfo]): Boolean = units.size < quantity.get
  override def accept(units: Iterable[FriendlyUnitInfo]): Boolean = units.size == quantity.get
}
