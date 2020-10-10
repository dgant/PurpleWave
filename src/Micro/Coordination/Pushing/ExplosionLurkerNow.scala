package Micro.Coordination.Pushing

import Mathematics.Points.Pixel
import ProxyBwapi.Bullets.BulletInfo

class ExplosionLurkerNow(spine: BulletInfo) extends LinearPush {
  override protected def source: Pixel = spine.pixel

  override protected def destination: Pixel = spine.targetPixel

  override protected def sourceWidth: Double = 20

  override val priority: Int = PushPriority.Shove
}
