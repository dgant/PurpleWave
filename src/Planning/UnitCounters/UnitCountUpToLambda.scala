package Planning.UnitCounters
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitCountUpToLambda(maximum: () => Int) extends UnitCounter {
  override def continue(units: Iterable[FriendlyUnitInfo]): Boolean = units.size < maximum()
  override def accept(units: Iterable[FriendlyUnitInfo]): Boolean = units.size <= maximum()
}
