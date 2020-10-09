package Micro.Coordination.Pushing
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class CircularPush(val priority: Integer, center: Pixel, radius: Double) extends Push {
  override def force(unit: FriendlyUnitInfo): Option[Force] = {
    val target = unit.pixelCenter
    val distance = target.pixelDistance(center)
    val magnitude = PurpleMath.nanToZero((radius - distance) / radius)
    val magnitudeClamped = PurpleMath.clamp(magnitude, 0, 1)
    if (magnitudeClamped <= 0) None else Some(ForceMath.fromPixels(center, target, magnitudeClamped))
  }
}
