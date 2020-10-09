package Micro.Coordination.Pushing

import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class LinearPush extends Push {
  protected def source: Pixel
  protected def destination: Pixel
  protected def sourceWidth: Double
  protected def range: Double
  protected def waypoint: Pixel = source.project(destination, Math.min(range, source.pixelDistance(destination)))

  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    val projection = PurpleMath.projectedPointOnSegment(recipient.pixelCenter.asPoint, source.asPoint, waypoint.asPoint).asPixel
    val distance = recipient.pixelDistanceCenter(projection) - sourceWidth - recipient.unitClass.dimensionMax
    val magnitude = PurpleMath.nanToZero((range - distance) / range)
    val magnitudeClamped = PurpleMath.clamp(magnitude, 0d, 1d)
    if (magnitudeClamped <= 0) return None
    Some(ForceMath.fromPixels(projection, recipient.pixelCenter, magnitudeClamped))
  }
}
