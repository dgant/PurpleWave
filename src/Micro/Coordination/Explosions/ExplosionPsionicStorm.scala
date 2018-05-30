package Micro.Coordination.Explosions

import Mathematics.Points.Pixel
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionPsionicStorm(psionicStorm: BulletInfo) extends ExplosionRadial {
  override def affects(unit: UnitInfo): Boolean = ! unit.unitClass.isBuilding
  override def framesRemaining: Double = psionicStorm.framesRemaining
  override def center: Pixel = psionicStorm.pixel
  override def radius: Double = 64 // Maybe wider?
}
