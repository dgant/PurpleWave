package Development.Visualization

import Development.Debugger
import Startup.With
import bwapi.{Color, UnitCommandType}

object VisualizeUnitsOurs {
  def render() {
    With.units.ours
      .filter(unit => Debugger.highlitUnits.contains(unit))
      .foreach(unit =>
        DrawMap.circle(unit.pixel, 32, Color.Orange))
    With.units.ours
      .filterNot(_.command.getUnitCommandType == UnitCommandType.None)
      .foreach(unit => DrawMap.labelBox(
        List(
          With.commander._lastIntentions.get(unit).map(intent => (intent.motivation * 100).toInt.toString).getOrElse(""),
          With.commander._lastIntentions.get(unit).map(intent => intent.plan.toString).getOrElse(""),
          With.commander._lastCommands.get(unit).getOrElse(""),
          unit.command.getUnitCommandType.toString),
        unit.pixel,
        drawBackground = false))
  }
}
