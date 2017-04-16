package Debugging.Visualizations.Views

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Micro.State.ExecutionState

import Utilities.EnrichPixel._

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
    if (state.movement.isDefined) {
      DrawMap.line(state.unit.pixelCenter, state.movement.get.pixelCenter, Colors.DarkGray)
    }
    if (state.target.isDefined) {
      DrawMap.line(state.unit.pixelCenter, state.intent.toAttack.get.pixelCenter, Colors.BrightRed)
    }
    if (state.intent.toGather.isDefined) {
      DrawMap.line(state.unit.pixelCenter, state.intent.toGather.get.pixelCenter, Colors.DarkGreen)
    }
    val targetUnit = state.unit.target.orElse(state.unit.orderTarget)
    if (targetUnit.nonEmpty) {
      DrawMap.line(state.unit.pixelCenter, targetUnit.get.pixelCenter, state.unit.player.colorNeon)
    }
    else {
      val targetPosition = state.unit.targetPosition.orElse(state.unit.orderTargetPosition)
      if (targetPosition.nonEmpty) {
        DrawMap.line(state.unit.pixelCenter, targetPosition.get, state.unit.player.colorDark)
      }
    }
  }
}
