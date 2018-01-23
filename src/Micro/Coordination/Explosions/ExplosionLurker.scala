package Micro.Coordination.Explosions

import Mathematics.Points.Pixel
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionLurker(spine: BulletInfo) extends ExplosionLine {
  override def thickness: Double = 20
  override def start: Pixel = spine.pixel
  override def end: Pixel = spine.sourceUnit
    .map(_.pixelCenter.project(start, 7.0 * 32))
    .getOrElse(spine.targetPixel)
  override def affects(unit: UnitInfo): Boolean = ! unit.flying
  override def framesRemaining: Double = 18
}
