package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Startup.With
import bwapi.UnitCommandType

object VisualizeUnitsOurs {
  
  def render() {
    
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
