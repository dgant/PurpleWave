package Micro.Coordination.Pushing

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Push {
  val priority: TrafficPriority
  def tiles: Seq[Tile]
  def force(recipient: FriendlyUnitInfo): Option[Force]

  def draw(): Unit

  def drawTiles(): Unit = {
    val t = tiles
    if (t.nonEmpty) {
      DrawMap.box(
        Pixel(
          t.view.map(_.x).min * 32,
          t.view.map(_.y).min * 32),
        Pixel(
          t.view.map(_.x).max * 32 + 31,
          t.view.map(_.y).max * 32 + 31),
        priority.color)
    }
  }

  protected def drawLabel(center: Pixel): Unit = {
    DrawMap.label(priority.toString, center, drawBackground = true, backgroundColor = priority.color)
  }

  override def toString: String = getClass.getSimpleName.replace("%", "").replace("Push", "")
}
