package Micro.Coordination.Explosions

import Mathematics.Points.Pixel
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionLurkerNow(spine: BulletInfo) extends ExplosionLine {
  override val thickness: Double = 20
  override val start: Pixel = spine.pixel
  override val end: Pixel = spine.sourceUnit
    .map(_.pixelCenter.project(start, 7.0 * 32))
    .getOrElse(spine.targetPixel)
  override def affects(unit: UnitInfo): Boolean = ! unit.flying
  override def framesRemaining: Double = 18
}
