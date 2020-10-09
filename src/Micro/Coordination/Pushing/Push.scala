package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Push {
  val priority: Integer
  def force(unit: FriendlyUnitInfo): Option[Force]
}
