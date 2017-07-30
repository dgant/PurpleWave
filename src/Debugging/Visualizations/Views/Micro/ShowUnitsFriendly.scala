package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Micro.Agency.Agent

object ShowUnitsFriendly extends View {
  
  override def renderMap() {
    With.agents.all.foreach(renderUnitState)
  }
  
  def renderUnitState(agent: Agent) {
    if ( ! With.viewport.contains(agent.unit.pixelCenter)) return
    if ( ! agent.unit.unitClass.orderable) return
    
    agent.lastClient.foreach(plan =>
      DrawMap.label(
        plan.toString,
        agent.unit.pixelCenter.add(0, -21),
        drawBackground = false))
    DrawMap.label(
      agent.lastAction.map(_.name).getOrElse(""),
      agent.unit.pixelCenter.add(0, -14),
      drawBackground = false)
    DrawMap.label(
      agent.unit.command.map(_.getUnitCommandType.toString).getOrElse(""),
      agent.unit.pixelCenter.add(0, -7),
      drawBackground = false)
    
    /*
        DrawMap.label(
      agent.unit.order.toString,
      agent.unit.pixelCenter.add(0, 0),
      drawBackground = false)
    */
    if (agent.movingTo.isDefined) {
      DrawMap.line(agent.unit.pixelCenter, agent.movingTo.get, Colors.MediumGray)
    }
    if (agent.toAttack.isDefined) {
      DrawMap.line(agent.unit.pixelCenter, agent.toAttack.get.pixelCenter, Colors.BrightRed)
    }
    if (agent.toForm.isDefined) {
      DrawMap.circle(agent.toForm.get, agent.unit.unitClass.radialHypotenuse.toInt, Colors.MediumTeal)
    }
    /*
    if (agent.toGather.isDefined) {
      DrawMap.line(agent.unit.pixelCenter, agent.intent.toGather.get.pixelCenter, Colors.DarkGreen)
    }
    val targetUnit = agent.unit.target.orElse(agent.unit.orderTarget)
    if (targetUnit.nonEmpty) {
      DrawMap.line(agent.unit.pixelCenter, targetUnit.get.pixelCenter, agent.unit.player.colorNeon)
    }
    else {
      val targetPosition = agent.unit.targetPixel.orElse(agent.unit.orderTargetPixel)
      if (targetPosition.nonEmpty && agent.unit.target.isEmpty) {
        DrawMap.line(agent.unit.pixelCenter, targetPosition.get, agent.unit.player.colorDark)
      }
    }
    
    */
  }
}
