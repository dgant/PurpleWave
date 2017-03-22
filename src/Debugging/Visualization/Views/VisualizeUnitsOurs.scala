package Debugging.Visualization.Views

import Debugging.Debugger
import Debugging.Visualization.Rendering.DrawMap
import Startup.With
import bwapi.{Color, UnitCommandType}

object VisualizeUnitsOurs {
  def render() {
    With.units.ours
      .filter(unit => Debugger.highlitUnits.contains(unit))
      .foreach(unit =>
        DrawMap.circle(unit.pixelCenter, 32, Color.Orange))
    With.units.ours
      .filterNot(_.command.getUnitCommandType == UnitCommandType.None)
      .foreach(unit => DrawMap.labelBox(
        List(
          With.executor.lastIntentions.get(unit).map(intent => intent.plan.toString).getOrElse(""),
          With.commander.lastCommands.get(unit).getOrElse(""),
          unit.command.getUnitCommandType.toString),
        unit.pixelCenter,
        drawBackground = false))
  }
}
