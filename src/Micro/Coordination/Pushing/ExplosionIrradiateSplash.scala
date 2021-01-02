package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class ExplosionIrradiateSplash(burningMan: UnitInfo) extends CircularPush(TrafficPriorities.Dodge, burningMan.pixel, 64) {
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    if (recipient == burningMan || ! recipient.unitClass.canBeIrradiateBurned) None else super.force(recipient)
  }
}
