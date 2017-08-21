package Planning.Composition.UnitCounters
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitCountTransportable(transports: Traversable[FriendlyUnitInfo]) extends UnitCounter {
  
  val spaceAvailable: Int = transports.map(_.spaceRemaining).sum
  
  override def continue(units: Iterable[FriendlyUnitInfo]): Boolean = {
    units.map(_.unitClass.spaceRequired).sum < spaceAvailable
  }
  
  override def accept(units: Iterable[FriendlyUnitInfo]): Boolean = {
    true
  }
}