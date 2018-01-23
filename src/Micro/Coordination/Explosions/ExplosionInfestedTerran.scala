package Micro.Coordination.Explosions

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionInfestedTerran(infestedTerran: UnitInfo) extends ExplosionRadial {
  override def center: Pixel = infestedTerran.orderTarget.map(_.pixelCenter).orElse(infestedTerran.orderTargetPixel).getOrElse(infestedTerran.projectFrames(24))
  override def radius: Double = 60
  override def affects(unit: UnitInfo): Boolean = ! unit.flying
  override def framesRemaining: Double = 24
}
