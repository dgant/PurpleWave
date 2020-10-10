package Micro.Coordination.Pushing

import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, PixelRay, Tile}
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class LinearPush extends Push {
  protected def source: Pixel
  protected def destination: Pixel
  protected def sourceWidth: Double // TODO: Not reflected in tiles!

  override val tiles: Seq[Tile] = PixelRay(source, destination).tilesIntersected
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    val projection = PurpleMath.projectedPointOnSegment(recipient.pixelCenter.asPoint, source.asPoint, destination.asPoint).asPixel
    val distance = recipient.pixelDistanceCenter(projection) - sourceWidth - recipient.unitClass.dimensionMax
    val length = source.pixelDistance(destination)
    val magnitude = PurpleMath.nanToZero((length - distance) / length)
    val magnitudeClamped = PurpleMath.clamp(magnitude, 0d, 1d)
    if (magnitudeClamped <= 0) return None
    Some(ForceMath.fromPixels(projection, recipient.pixelCenter, magnitudeClamped))
  }
}
