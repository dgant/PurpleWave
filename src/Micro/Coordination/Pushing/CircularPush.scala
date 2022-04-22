package Micro.Coordination.Pushing
import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, Tile}
import Mathematics.Maff
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class CircularPush(val priority: TrafficPriority, center: Pixel, radius: Double, originators: FriendlyUnitInfo*) extends Push {
  override val tiles: Seq[Tile] = Circle((radius.toInt + 31) / 32).view.map(center.tile.add)
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    if (originators.contains(recipient)) return None
    val target = recipient.pixel
    val distance = target.pixelDistance(center)
    val magnitude = Maff.nanToZero((radius - distance) / radius)
    val magnitudeClamped = Maff.clamp(magnitude, 0, 1)
    if (magnitudeClamped <= 0) None else Some(ForceMath.fromPixels(center, target, magnitudeClamped))
  }

  override def draw(): Unit = {
    val color = priority.color
    drawLabel(center)
    DrawMap.circle(center, radius.toInt, color)
  }
}
