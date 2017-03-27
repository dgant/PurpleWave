package Debugging.Visualization.Views

import Debugging.Visualization.Colors
import Debugging.Visualization.Rendering.DrawMap
import Startup.With

object VisualizeUnitsOurs {
  
  def render() {
    
    With.units.ours
      .foreach(unit => {
        val intent = With.executor.lastIntentions.get(unit)
        DrawMap.labelBox(
          List(
            intent.map(intent => intent.plan.toString).getOrElse(""),
            With.commander.lastCommands.get(unit).getOrElse(""),
            unit.command.getUnitCommandType.toString),
          unit.pixelCenter,
          drawBackground = false)
        
        if (intent.exists(_.toGather.isDefined)) {
          DrawMap.line(unit.pixelCenter, intent.get.toGather.get.pixelCenter, Colors.BrightGreen)
        }
      })
  }
}
