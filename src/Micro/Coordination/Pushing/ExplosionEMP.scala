package Micro.Coordination.Pushing

import Mathematics.Physics.Force
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class ExplosionEMP(emp: BulletInfo) extends CircularPush(TrafficPriorities.Dodge, emp.targetPixel, 96) {
  override def force(unit: FriendlyUnitInfo): Option[Force] = {
    if(unit.energy <= 5 && unit.shieldPoints == 20) None else super.force(unit)
  }
}
