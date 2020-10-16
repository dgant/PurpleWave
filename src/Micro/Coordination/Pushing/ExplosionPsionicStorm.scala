package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class ExplosionPsionicStorm(psionicStorm: BulletInfo) extends CircularPush(TrafficPriorities.Dodge, psionicStorm.pixel, 64) {
  override def force(recpient: FriendlyUnitInfo): Option[Force] = {
    if (recpient.unitClass.isBuilding) None else super.force(recpient)
  }
}
