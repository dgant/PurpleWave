package Plans.Generic.Allocation
import bwapi.Unit

object LockUnitsNobody extends LockUnits {
  override def getRequiredUnits(candidates: Iterable[Iterable[Unit]]): Option[Iterable[Unit]] = { Some(List.empty) }
}
