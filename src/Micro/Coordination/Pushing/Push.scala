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
    if (tiles.nonEmpty) {
      DrawMap.box(
        Pixel(
          tiles.view.map(_.x).min * 32,
          tiles.view.map(_.y).min * 32),
        Pixel(
          31 + tiles.view.map(_.x).max * 32,
          31 + tiles.view.map(_.y).max * 32),
        priority.color)
    }
  }

  protected def drawLabel(center: Pixel): Unit = {
    DrawMap.label(toString, center, drawBackground = true, backgroundColor = priority.color)
  }

  override val toString: String = getClass.getSimpleName.replace("%", "")
}
