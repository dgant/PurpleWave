package Micro.Coordination.Explosions

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Mathematics.Points.Pixel
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionEMP(emp: BulletInfo) extends ExplosionRadial {
  override def center: Pixel = emp.targetPixel
  override def radius: Double = 96.0
  override def affects(unit: UnitInfo): Boolean = unit.energy > 0 || unit.shieldPoints > 0
  override def framesRemaining: Double = GameTime(0, 1)()
}
