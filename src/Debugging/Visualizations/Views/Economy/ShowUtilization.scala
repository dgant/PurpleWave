package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import bwapi.Color

object ShowUtilization extends View {
  val max = Math.log(24 * 60)
  override def renderMap(): Unit = {
    With.units.ours
      .filter(u => u.unitClass.unitsTrained.nonEmpty || u.unitClass.techsWhat.nonEmpty || u.unitClass.upgradesWhat.nonEmpty)
      .filterNot(_.canMove)
      .foreach(unit => {
        val frames = With.framesSince(unit.lastFrameOccupied)
        if (frames > 0) {
          val ratio = Math.min(1.0, Math.log(frames) / max)
          DrawMap.box(
            unit.pixel.subtract((ratio * unit.unitClass.dimensionLeft).toInt, (ratio * unit.unitClass.dimensionUp).toInt),
            unit.pixel.add((ratio * unit.unitClass.dimensionRight).toInt, (ratio * unit.unitClass.dimensionDown).toInt),
            Color.Black,
            solid = true)
        }})
  }
}
