package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Debugging.Visualizations.{Colors, ForceColors}
import Lifecycle.With
import Micro.Agency.Agent
import Utilities.ByOption
import bwapi.Color

object ShowUnitsFriendly extends View {
  
  var showClient      : Boolean = true
  var showAction      : Boolean = true
  var showCommand     : Boolean = false
  var showOrder       : Boolean = false
  var showTargets     : Boolean = false
  var showFormation   : Boolean = true
  var showKiting      : Boolean = false
  var showForces      : Boolean = true
  var showDesire      : Boolean = true
  var showExplosions  : Boolean = true
  
  override def renderMap() { With.agents.all.foreach(renderUnitState) }
  
  def renderUnitState(agent: Agent) {
    if ( ! With.viewport.contains(agent.unit.pixelCenter)) return
    if ( ! agent.unit.unitClass.orderable) return
    if (agent.unit.transport.isDefined) return
    
    if (showClient) {
      agent.lastClient.foreach(plan =>
        DrawMap.label(
          plan.toString,
          agent.unit.pixelCenter.add(0, -21),
          drawBackground = false))
    }
    if (showAction) {
      DrawMap.label(
        agent.lastAction.map(_.name).getOrElse(""),
        agent.unit.pixelCenter.add(0, -14),
        drawBackground = false)
    }
    if (showCommand) {
      DrawMap.label(
        agent.unit.command.map(_.getUnitCommandType.toString).getOrElse(""),
        agent.unit.pixelCenter.add(0, -7),
        drawBackground = false)
    }
    if (showOrder) {
      DrawMap.label(
        agent.unit.order.toString,
        agent.unit.pixelCenter.add(0, 0),
        drawBackground = false)
    }
    
    if (showTargets) {
      val targetUnit = agent.unit.target.orElse(agent.unit.orderTarget)
      if (targetUnit.nonEmpty) {
        DrawMap.line(agent.unit.pixelCenter, targetUnit.get.pixelCenter, agent.unit.player.colorNeon)
      }
      val targetPosition = agent.unit.targetPixel.orElse(agent.unit.orderTargetPixel)
      if (targetPosition.nonEmpty && agent.unit.target.isEmpty) {
        DrawMap.arrow(agent.unit.pixelCenter, targetPosition.get, agent.unit.player.colorDark)
      }
      if (agent.movingTo.isDefined) {
        DrawMap.arrow(agent.unit.pixelCenter, agent.movingTo.get, Colors.MediumGray)
      }
      if (agent.toAttack.isDefined) {
        DrawMap.arrow(agent.unit.pixelCenter, agent.toAttack.get.pixelCenter, Colors.BrightRed)
      }
      if (agent.toGather.isDefined) {
        DrawMap.arrow(agent.unit.pixelCenter, agent.toGather.get.pixelCenter, Colors.DarkGreen)
      }
    }
    if (showFormation) {
      if (agent.toForm.isDefined) {
        DrawMap.circle(agent.toForm.get, agent.unit.unitClass.radialHypotenuse.toInt, Colors.DarkTeal)
      }
    }
    
    if (showKiting) {
      agent.pathsAll.foreach(ray => ray.tilesIntersected.foreach(tile => DrawMap.box(
        tile.topLeftPixel.add(1, 1),
        tile.bottomRightPixel.subtract(1, 1),
        if (With.grids.walkable.get(tile)) Colors.BrightBlue else Colors.BrightRed)))
      agent.pathsAll.foreach(ray => DrawMap.line(ray.from, ray.to, Colors.MediumGray))
      agent.pathsTruncated.foreach(ray => DrawMap.line(ray.from, ray.to, Colors.MediumGreen))
      agent.pathsAcceptable.foreach(ray => DrawMap.line(ray.from, ray.to, Colors.BrightGreen))
      agent.pathAccepted.foreach(ray => { DrawMap.line(ray.from, ray.to, Colors.NeonGreen); DrawMap.circle(ray.to, 4, Colors.NeonGreen, solid = true) })
    }
    
    if (showForces) {
      val length = 96.0
      val maxForce = ByOption.max(agent.forces.values.map(_.lengthSlow)).getOrElse(0.0)
      if (maxForce > 0.0) {
        agent.forces.foreach(pair => {
          val force           = pair._2
          val forceNormalized = force.normalize(length * force.lengthSlow / maxForce)
          DrawMap.arrow(
            agent.unit.pixelCenter,
            agent.unit.pixelCenter.add(
              forceNormalized.x.toInt,
              forceNormalized.y.toInt),
            pair._1)
        })
        if (agent.movingTo.isDefined) {
          DrawMap.arrow(
            agent.unit.pixelCenter,
            agent.movingTo.get,
            ForceColors.sum)
        }
      }
    }
    
    if (showDesire) {
      val color = if (agent.shouldEngage) Colors.NeonGreen else Colors.NeonRed
      val pixel = agent.unit.pixelCenter.subtract(0, 6 + agent.unit.unitClass.height / 2)
      DrawMap.circle(pixel, 3, Color.Black, solid = true)
      DrawMap.circle(pixel, 2, color,       solid = true)
    }
    
    if (showExplosions) {
      agent.explosions.foreach(explosion => {
        DrawMap.circle(explosion.pixelCenter, explosion.safetyRadius.toInt, Colors.NeonYellow)
      })
    }
  }
}
