package Micro.Coordination.Explosions

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

abstract class ExplosionLine extends Explosion {
  
  def thickness: Double
  def start: Pixel
  def end: Pixel
  
  protected val length = start.pixelDistanceSlow(end)
  protected val angle = start.radiansTo(end)
  protected val p0 = start.radiateRadians(angle - Math.PI / 2, thickness)
  protected val p1 = start.radiateRadians(angle + Math.PI / 2, thickness)
  protected val p2 = start.radiateRadians(angle + Math.PI / 2, thickness)
  protected val p3 = start.radiateRadians(angle - Math.PI / 2, thickness)
  
  override def draw(): Unit = {
    DrawMap.polygonPixels(Iterable(p0, p1, p2, p3), color)
  }
  
  def pixelsOfEntanglement(unit: UnitInfo): Double = {
    unit.unitClass.radialHypotenuse + thickness - PurpleMath.distanceFromLineSegment(unit.pixelCenter, start, end)
  }
  
  override def directionTo(unit: UnitInfo): Force = {
    val pixelsFromStart = unit.pixelDistanceFast(start)
    val pixelsFromEnd = unit.pixelDistanceFast(end)
    if (pixelsFromEnd > length) {
      return new Force(unit.pixelCenter.subtract(start))
    }
    if (pixelsFromStart > length) {
      return new Force(unit.pixelCenter.subtract(end))
    }
    // TODO: Actual math
    new Force(unit.pixelCenter.subtract(start.midpoint(end)))
  }
}
