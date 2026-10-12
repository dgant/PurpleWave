package Micro.Formation

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

private[Formation] class GuardGeometry(chokeCenter: Pixel, inward: Pixel, radius: Double, buildings: Iterable[UnitInfo]) {
  val inwardRadians: Double = chokeCenter.radiansToSlow(inward)
  val defenses: Vector[UnitInfo] = buildings.view
    .filter(_.unitClass.isStaticDefense)
    .filter(b => b.pixelDistanceEdge(chokeCenter) < b.pixelRangeMax + radius)
    .toVector

  def covered(pixel: Pixel): Boolean = defenses.forall(b => b.pixelDistanceEdge(pixel) <= b.pixelRangeMax)

  // Stop at the first point in every building's range, or at the choke-radius limit.
  val center: Pixel = (0 to Math.ceil(radius).toInt).iterator
    .map(distance => chokeCenter.radiateRadians(inwardRadians, Math.min(distance, radius)))
    .find(covered)
    .getOrElse(chokeCenter.radiateRadians(inwardRadians, radius))

  def facesOutward(pixel: Pixel): Boolean =
    (pixel.x - center.x).toDouble * (inward.x - chokeCenter.x) +
      (pixel.y - center.y).toDouble * (inward.y - chokeCenter.y) < 0

  def apex(depth: Double): Pixel = center.radiateRadians(inwardRadians, depth)
  def face: Pixel = center.radiateRadians(inwardRadians, -radius)
}
