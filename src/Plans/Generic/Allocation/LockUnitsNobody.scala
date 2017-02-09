package Plans.Generic.Allocation
import Startup.With
import bwapi.Unit

object LockUnitsNobody extends LockUnits {
  override def getRequiredUnits(candidates: Iterable[Iterable[Unit]]): Option[Iterable[Unit]] = {
    With.logger.warn("LockUnitsNobody was actually offered units.")
    Some(List.empty)
  }
}
