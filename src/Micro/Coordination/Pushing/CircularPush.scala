package Micro.Coordination.Pushing
import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, PixelRectangle, Tile}
import Mathematics.Maff
import Micro.Heuristics.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class CircularPush(val priority: TrafficPriority, center: Pixel, radius: Double, originators: FriendlyUnitInfo*) extends Push {
  // Tile lookup must cover the pixel radius even when the center is not tile-aligned.
  override val tiles: Seq[Tile] = new PixelRectangle(center).expand(Math.ceil(radius).toInt, Math.ceil(radius).toInt).tiles.tiles
  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    if (originators.contains(recipient)) return None
    val target = recipient.pixel
    val distance = target.pixelDistance(center)
    if (center == target) return Some(Potential.towards(recipient, recipient.agent.safety))
    val magnitude = Maff.nanToZero((radius - distance) / radius)
    val magnitudeClamped = Maff.clamp01(magnitude)
    if (magnitudeClamped <= 0) None else Some(ForceMath.fromPixels(center, target, magnitudeClamped))
  }

  override def draw(): Unit = {
    val color = priority.color
    drawLabel(center)
    DrawMap.circle(center, radius.toInt, color)
  }
}
