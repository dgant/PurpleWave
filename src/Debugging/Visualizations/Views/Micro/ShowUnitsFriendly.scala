package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Execution.ActionState

object ShowUnitsFriendly extends View {
  
  override def renderMap() {
    With.executor.states.foreach(renderUnitState)
  }
  
  def renderUnitState(state: ActionState) {
    if ( ! With.viewport.contains(state.unit.pixelCenter)) return
    
    if (state.intent.plan != With.strategy.gameplan) {
      DrawMap.label(
        state.intent.plan.toString,
        state.unit.pixelCenter.add(0, -21),
        drawBackground = false)
    }
    DrawMap.label(
      state.lastAction.map(_.name).getOrElse(""),
      state.unit.pixelCenter.add(0, -14),
      drawBackground = false)
    DrawMap.label(
      state.unit.command.map(_.getUnitCommandType.toString).getOrElse(""),
      state.unit.pixelCenter.add(0, -7),
      drawBackground = false)
    
    if (state.desireTotal != 1.0) {
      val x = state.unit.pixelCenter.x
      val y = state.unit.pixelCenter.y + 14
      val width = 15
      drawDesire(state.desireTeam,        x - width * 3 / 2,  y, width)
      drawDesire(state.desireIndividual,  x - width     / 2,  y, width)
      drawDesire(state.desireTotal,       x + width     / 2,  y, width)
    }
    
    if (state.unit.battle.nonEmpty) {
      val topRight = Pixel(state.unit.right, state.unit.top)
      DrawMap.label("%1.2f".format(state.unit.matchups.vpfDealingDiffused),     topRight,                                             drawBackground = true, backgroundColor = With.self.colorMedium)
      DrawMap.label("%1.2f".format(state.unit.matchups.vpfReceivingDiffused),   topRight.add(0, With.visualization.lineHeightSmall),  drawBackground = true, backgroundColor = With.enemy.colorMedium)
    }
    /*
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
  
  def drawDesire(desire: Double, x: Int, y: Int, width: Int) {
    val color = if (desire == 1.0) Colors.MediumGray else if (desire > 1.0) Colors.DarkGreen else Colors.MediumRed
    DrawMap.box(
      Pixel(x, y),
      Pixel(x + width, y + With.visualization.lineHeightSmall + 2),
      solid = true,
      color = color)
    DrawMap.text(Pixel(x + 2, y), "%1.1f".format(desire).replace("Infinity", "Inf"))
  }
}
