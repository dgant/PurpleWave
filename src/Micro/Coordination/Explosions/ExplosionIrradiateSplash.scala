package Micro.Coordination.Explosions

import Information.Fingerprinting.Generic.GameTime
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionIrradiateSplash(burningMan: UnitInfo) extends ExplosionRadial {
  override def center: Pixel = burningMan.pixelCenter
  override def radius: Double = 64.0
  override def affects(unit: UnitInfo): Boolean = unit != burningMan && unit.unitClass.canBeIrradiateBurned
  override def framesRemaining: Double = GameTime(0, 5)()
}
