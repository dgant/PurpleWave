package Micro.Coordination.Explosions

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionScarab(scarab: UnitInfo) extends ExplosionRadial {
  override def center: Pixel = scarab.orderTarget.map(_.pixelCenter).orElse(scarab.orderTargetPixel).getOrElse(scarab.projectFrames(24))
  override def radius: Double = 60
  override def affects(unit: UnitInfo): Boolean = ! unit.flying
  override def framesRemaining: Double = 24
}
