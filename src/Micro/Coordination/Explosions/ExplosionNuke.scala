package Micro.Coordination.Explosions

import Information.Fingerprinting.Generic.GameTime
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionNuke(pixel: Pixel) extends ExplosionRadial {
  override def center: Pixel = pixel
  override def radius: Double = 256.0
  override def affects(unit: UnitInfo): Boolean = true
  override def framesRemaining: Double = GameTime(0, 17)()
}
