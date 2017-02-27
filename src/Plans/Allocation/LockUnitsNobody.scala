package Plans.Allocation
import Startup.With
import Types.UnitInfo.FriendlyUnitInfo

object LockUnitsNobody extends LockUnits {
  override def getRequiredUnits(candidates: Iterable[Iterable[FriendlyUnitInfo]]): Option[Iterable[FriendlyUnitInfo]] = {
    With.logger.warn("LockUnitsNobody was actually offered units.")
    Some(List.empty)
  }
}
