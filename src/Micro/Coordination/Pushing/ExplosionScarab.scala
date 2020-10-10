package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class ExplosionScarab(scarab: UnitInfo) extends CircularPush(
  PushPriority.Dodge,
  scarab.orderTarget.map(_.pixelCenter).orElse(scarab.orderTargetPixel).getOrElse(scarab.projectFrames(24)),
  60) {
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    if (recipient.flying) None else super.force(recipient)
  }
}
