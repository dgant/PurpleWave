package Micro.Coordination.Pushing
import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, Tile}
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class CircularPush(val priority: TrafficPriority, center: Pixel, radius: Double) extends Push {
  override val tiles: Seq[Tile] = Circle.points((radius.toInt + 31) / 32).view.map(center.tileIncluding.add)
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    val target = recipient.pixelCenter
    val distance = target.pixelDistance(center)
    val magnitude = PurpleMath.nanToZero((radius - distance) / radius)
    val magnitudeClamped = PurpleMath.clamp(magnitude, 0, 1)
    if (magnitudeClamped <= 0) None else Some(ForceMath.fromPixels(center, target, magnitudeClamped))
  }

  override def draw(): Unit = {
    val color = priority.color
    drawLabel(center)
    DrawMap.circle(center, radius.toInt, color)
  }
}