package Micro.Coordination.Pushing

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Maff
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class LinearPush extends Push {
  protected def source: Pixel
  protected def destination: Pixel
  protected def sourceWidth: Double // TODO: Not reflected in tiles!

  override def tiles: Seq[Tile] = new TileRectangle(corners.view.map(_.tile)).tiles
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    val projection = Maff.projectedPointOnSegment(recipient.pixel, source, destination)
    val distance = recipient.pixelDistanceCenter(projection) - sourceWidth - recipient.unitClass.dimensionMax
    val length = source.pixelDistance(destination)
    val magnitude = Maff.nanToZero((length - distance) / length)
    val magnitudeClamped = Maff.clamp(magnitude, 0d, 1d)
    if (magnitudeClamped <= 0) return None
    Some(ForceMath.fromPixels(projection, recipient.pixel, magnitudeClamped))
  }

  def corners: Vector[Pixel] = {
    val radians = source.radiansTo(destination)
    Vector(
      source      .radiateRadians(radians + Math.PI / 2, sourceWidth),
      source      .radiateRadians(radians - Math.PI / 2, sourceWidth),
      destination .radiateRadians(radians - Math.PI / 2, sourceWidth),
      destination .radiateRadians(radians + Math.PI / 2, sourceWidth))
  }

  override def draw(): Unit = {
    DrawMap.polygonPixels(corners,  priority.color)
  }
}
