package Debugging.Visualization.Views

import Debugging.Visualization.Colors
import Debugging.Visualization.Rendering.DrawMap
import Lifecycle.With
import Utilities.EnrichPosition._

object VisualizeUnitsOurs {
  
  def render() {
    
    With.units.ours
      .foreach(unit => {
        val intent = With.executor.lastIntentions.get(unit)
        DrawMap.label(
          intent.map(intent => intent.plan.toString).getOrElse(""),
          unit.pixelCenter.add(0, -7),
          drawBackground = false)
        DrawMap.label(
          unit.command.getUnitCommandType.toString,
          unit.pixelCenter.add(0, +7),
          drawBackground = false)
        if (intent.exists(_.destination.isDefined)) {
          DrawMap.line(unit.pixelCenter, intent.get.destination.get.pixelCenter, Colors.DarkGray)
        }
        if (intent.exists(_.toAttack.isDefined)) {
          DrawMap.line(unit.pixelCenter, intent.get.toAttack.get.pixelCenter, Colors.DarkRed)
        }
        if (intent.exists(_.toGather.isDefined)) {
          DrawMap.line(unit.pixelCenter, intent.get.toGather.get.pixelCenter, Colors.DarkGreen)
        }
      })
  }
}
