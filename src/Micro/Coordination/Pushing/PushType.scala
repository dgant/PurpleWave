package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait PushType {
  val priority: Integer
  def force(unit: FriendlyUnitInfo): Option[Force]
}
