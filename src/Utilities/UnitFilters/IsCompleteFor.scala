package Utilities.UnitFilters
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

case class IsCompleteFor(ageFrames: Int) extends UnitFilter{
  override def apply(unit: UnitInfo): Boolean = With.framesSince(unit.completionFrame) >= ageFrames
}
