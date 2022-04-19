package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowUtilization extends DebugView {
  val max: Double = Math.log(24 * 60)
  override def renderMap(): Unit = {
    // Production utilization
    With.units.ours
      .filter(_.unitClass.trainsUpgradesOrTechs)
      .filterNot(_.canMove)
      .foreach(unit => {
        val frames = With.framesSince(unit.lastFrameOccupied)
        if (frames > 0) {
          renderUnit(unit, Math.min(1.0, Math.log(frames) / max))
        }})

    // Resource saturation
    (With.units.neutral ++ With.units.ours)
      .filter(_.complete)
      .filter(_.unitClass.isResource)
      .filter(u => u.isOurs || u.base.exists(_.owner.isUs))
      .filter(u => With.framesSince(u.lastFrameHarvested) > 0)
      .foreach(u => renderUnit(u, Maff.clamp(With.framesSince(u.lastFrameHarvested) / 24.0, 0.0, 1.0)))

    val gathererCounts = With.units.ours.view
      .filter(_.agent.toGather.isDefined)
      .groupBy(_.agent.toGather.get)
      .map(p => (p._1, p._2.size))
    gathererCounts.foreach(c => DrawMap.label(c._2.toString, c._1.pixel.add(0, 8)))
  }

  def renderUnit(unit: UnitInfo, ratio: Double): Unit = {
    if (ratio <= 0) return
    DrawMap.box(
      unit.topLeft,
      unit.bottomRight,
      Color.Black)
    DrawMap.box(
      unit.topLeft.add(1, 1),
      unit.bottomRight.subtract(1, 1),
      Color.Black)
    DrawMap.box(
      unit.pixel.subtract((ratio * unit.unitClass.dimensionLeft).toInt, (ratio * unit.unitClass.dimensionUp).toInt),
      unit.pixel.add((ratio * unit.unitClass.dimensionRight).toInt, (ratio * unit.unitClass.dimensionDown).toInt),
      Color.Black,
      solid = true)
  }
}
