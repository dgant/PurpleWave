package Strategies.UnitCounters

import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property

class UnitCountGreedily(var originalMinimum:Int = 1) extends UnitCounter {
  
  val minimum = new Property[Int](originalMinimum)
  
  override def continue(units: Iterable[FriendlyUnitInfo]): Boolean = true
  override def accept(units: Iterable[FriendlyUnitInfo]): Boolean = units.size >= minimum.get
}
