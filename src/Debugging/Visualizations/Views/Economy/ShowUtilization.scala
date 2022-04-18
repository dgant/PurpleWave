package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import bwapi.Color

object ShowUtilization extends DebugView {
  val max: Double = Math.log(24 * 60)
  override def renderMap(): Unit = {
    With.units.ours
      .filter(_.unitClass.trainsUpgradesOrTechs)
      .filterNot(_.canMove)
      .foreach(unit => {
        val frames = With.framesSince(unit.lastFrameOccupied)
        if (frames > 0) {
          val ratio = Math.min(1.0, Math.log(frames) / max)
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
        }})
  }
}
