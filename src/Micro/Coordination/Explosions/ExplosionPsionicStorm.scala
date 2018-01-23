package Micro.Coordination.Explosions

import Mathematics.Points.Pixel
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionPsionicStorm(psionicStorm: BulletInfo) extends ExplosionRectangular {
  override def start: Pixel = psionicStorm.pixel.subtract(48, 48)
  override def end: Pixel = psionicStorm.pixel.add(48, 48)
  override def affects(unit: UnitInfo): Boolean = ! unit.unitClass.isBuilding
  override def framesRemaining: Double = psionicStorm.framesRemaining
}
