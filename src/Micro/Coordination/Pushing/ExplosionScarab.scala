package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class ExplosionScarab(scarab: UnitInfo) extends CircularPush(
  TrafficPriorities.Dodge,
  // In practice, both orderTarget and orderTargetPixel tend to be populated
  // (and the orderTargetPixel is on the edge of the unit)
  scarab.orderTargetPixel
    .orElse(scarab.orderTarget.map(_.pixel.project(scarab.pixel, 3)))
    .getOrElse(scarab.projectFrames(24)),
  60) {
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    if (recipient.flying) None
    else if (scarab.damageOnNextHitAgainst(recipient) > recipient.totalHealth && (scarab.orderTarget.contains(recipient) || scarab.orderTargetPixel.contains(recipient.pixel))) None
    else super.force(recipient)
  }
}
