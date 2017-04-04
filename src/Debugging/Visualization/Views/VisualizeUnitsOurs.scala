package Debugging.Visualization.Views

import Debugging.Visualization.Colors
import Debugging.Visualization.Rendering.DrawMap
import Lifecycle.With
import Micro.State.ExecutionState

import Utilities.EnrichPosition._

object VisualizeUnitsOurs {
  
  def render() = {
    With.executor.states.foreach(renderUnitState)
  }
  
  def renderUnitState(state: ExecutionState) {
    DrawMap.label(
      state.intent.plan.toString,
      state.unit.pixelCenter.add(0, -7),
      drawBackground = false)
    DrawMap.label(
      state.unit.command.getUnitCommandType.toString,
      state.unit.pixelCenter.add(0, +7),
      drawBackground = false)
    if (state.intent.destination.isDefined) {
      DrawMap.line(state.unit.pixelCenter, state.intent.destination.get.pixelCenter, Colors.DarkGray)
    }
    if (state.intent.toAttack.isDefined) {
      DrawMap.line(state.unit.pixelCenter, state.intent.toAttack.get.pixelCenter, Colors.BrightRed)
    }
    if (state.intent.toGather.isDefined) {
      DrawMap.line(state.unit.pixelCenter, state.intent.toGather.get.pixelCenter, Colors.DarkGreen)
    }
  }
}
