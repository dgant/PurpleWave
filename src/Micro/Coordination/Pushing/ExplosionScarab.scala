package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class ExplosionScarab(scarab: UnitInfo) extends CircularPush(
  TrafficPriorities.Dodge,
  scarab.orderTarget.map(_.pixel).orElse(scarab.orderTargetPixel).getOrElse(scarab.projectFrames(24)),
  60) {
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    if (recipient.flying) None else super.force(recipient)
  }
}
