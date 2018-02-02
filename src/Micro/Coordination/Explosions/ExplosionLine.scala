package Micro.Coordination.Explosions

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

abstract class ExplosionLine extends Explosion {
  
  val thickness: Double
  val start: Pixel
  val end: Pixel
  
  protected lazy val length = start.pixelDistanceSlow(end)
  protected lazy val angle = start.radiansTo(end)
  protected lazy val p0 = start.radiateRadians(angle - Math.PI / 2, thickness)
  protected lazy val p1 = start.radiateRadians(angle + Math.PI / 2, thickness)
  protected lazy val p2 = end.radiateRadians(angle + Math.PI / 2, thickness)
  protected lazy val p3 = end.radiateRadians(angle - Math.PI / 2, thickness)
  
  override def draw(): Unit = {
    DrawMap.polygonPixels(Iterable(p0, p1, p2, p3), color)
  }
  
  def pixelsOfEntanglement(unit: UnitInfo): Double = {
    unit.unitClass.radialHypotenuse + thickness - PurpleMath.distanceFromLineSegment(unit.pixelCenter, start, end)
  }
  
  override def directionTo(unit: UnitInfo): Force = {
    val pixelsFromStart = unit.pixelDistanceCenter(start)
    val pixelsFromEnd = unit.pixelDistanceCenter(end)
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
