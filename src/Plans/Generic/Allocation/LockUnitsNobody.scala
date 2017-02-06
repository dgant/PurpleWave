package Plans.Generic.Allocation
import Development.Logger
import bwapi.Unit

object LockUnitsNobody extends LockUnits {
  override def getRequiredUnits(candidates: Iterable[Iterable[Unit]]): Option[Iterable[Unit]] = {
    Logger.warn("LockUnitsNobody was actually offered units.")
    Some(List.empty)
  }
}
