package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Micro.Execution.ActionState

object ShowUnitsFriendly extends View {
  
  override def renderMap() {
    With.executor.states.foreach(renderUnitState)
  }
  
  def renderUnitState(state: ActionState) {
    if ( ! With.viewport.contains(state.unit.pixelCenter)) return
    
    if (state.intent.plan != With.gameplan) {
      DrawMap.label(
        state.intent.plan.toString,
        state.unit.pixelCenter.add(0, -21),
        drawBackground = false)
    }
    DrawMap.label(
      state.lastAction.map(_.name).getOrElse(""),
      state.unit.pixelCenter.add(0, -14),
      drawBackground = false)
    /*
    DrawMap.label(
      state.unit.command.map(_.getUnitCommandType.toString).getOrElse(""),
      state.unit.pixelCenter.add(0, -7),
      drawBackground = false)
    DrawMap.label(
      state.unit.order.toString,
      state.unit.pixelCenter.add(0, 0),
      drawBackground = false)
    */
    /*
    if (state.movingTo.isDefined) {
      DrawMap.line(state.unit.pixelCenter, state.movingTo.get, Colors.MediumGray)
    }
    if (state.target.isDefined) {
      DrawMap.line(state.unit.pixelCenter, state.intent.toAttack.get.pixelCenter, Colors.BrightRed)
    }
    */
    if (state.toForm.isDefined) {
      DrawMap.circle(state.toForm.get, state.unit.unitClass.radialHypotenuse.toInt, Colors.MediumTeal)
    }
    /*
    if (state.toGather.isDefined) {
      DrawMap.line(state.unit.pixelCenter, state.intent.toGather.get.pixelCenter, Colors.DarkGreen)
    }
    val targetUnit = state.unit.target.orElse(state.unit.orderTarget)
    if (targetUnit.nonEmpty) {
      DrawMap.line(state.unit.pixelCenter, targetUnit.get.pixelCenter, state.unit.player.colorNeon)
    }
    else {
      val targetPosition = state.unit.targetPixel.orElse(state.unit.orderTargetPixel)
      if (targetPosition.nonEmpty && state.unit.target.isEmpty) {
        DrawMap.line(state.unit.pixelCenter, targetPosition.get, state.unit.player.colorDark)
      }
    }
    
    */
  }
}
