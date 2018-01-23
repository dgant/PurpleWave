package Micro.Coordination.Explosions

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionIrradiate(burningMan: UnitInfo) extends ExplosionRadial {
  override def center: Pixel = burningMan.pixelCenter
  override def radius: Double = 64.0
  override def affects(unit: UnitInfo): Boolean = unit != burningMan && unit.unitClass.isOrganic && ! unit.unitClass.isBuilding
  override def framesRemaining: Double = GameTime(0, 5)()
}
