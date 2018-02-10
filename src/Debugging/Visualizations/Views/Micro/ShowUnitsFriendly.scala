package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Debugging.Visualizations.{Colors, ForceColors}
import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption
import bwapi.Color

object ShowUnitsFriendly extends View {
  
  var selectedOnly    : Boolean = false
  var showClient      : Boolean = true
  var showAction      : Boolean = true
  var showCommand     : Boolean = false
  var showOrder       : Boolean = false
  var showTargets     : Boolean = true
  var showFormation   : Boolean = true
  var showKiting      : Boolean = false
  var showForces      : Boolean = false
  var showDesire      : Boolean = true
  
  override def renderMap() { With.units.ours.foreach(renderUnitState) }
  
  def renderUnitState(unit: FriendlyUnitInfo) {
    val agent = unit.agent
    if (selectedOnly && ! unit.selected) return
    if ( ! unit.aliveAndComplete && ! unit.unitClass.isBuilding) return
    if ( ! With.viewport.contains(unit.pixelCenter)) return
    if ( ! unit.unitClass.orderable) return
    if (unit.transport.isDefined) return
    
    if (showClient) {
      agent.lastClient.foreach(plan =>
        DrawMap.label(
          plan.toString,
          unit.pixelCenter.add(0, -21),
          drawBackground = false))
    }
    if (showAction) {
      DrawMap.label(
        agent.lastAction.map(_.name).getOrElse(""),
        unit.pixelCenter.add(0, -14),
        drawBackground = false)
    }
    if (showCommand) {
      DrawMap.label(
        unit.command.map(_.getUnitCommandType.toString).getOrElse(""),
        unit.pixelCenter.add(0, -7),
        drawBackground = false)
    }
    if (showOrder) {
      DrawMap.label(
        unit.order.toString,
        unit.pixelCenter.add(0, 0),
        drawBackground = false)
    }
    
    if (showTargets) {
      val targetUnit = unit.target.orElse(unit.orderTarget)
      if (targetUnit.nonEmpty) {
        DrawMap.line(unit.pixelCenter, targetUnit.get.pixelCenter, unit.player.colorNeon)
      }
      val targetPosition = unit.targetPixel.orElse(unit.orderTargetPixel)
      if (targetPosition.nonEmpty && unit.target.isEmpty) {
        DrawMap.arrow(unit.pixelCenter, targetPosition.get, unit.player.colorDark)
      }
      if (agent.movingTo.isDefined) {
        if (selectedOnly) {
          DrawMap.arrow(unit.pixelCenter, agent.nextWaypoint(agent.movingTo.get), Colors.BrightGray)
        }
        DrawMap.arrow(unit.pixelCenter, agent.movingTo.get, Colors.MidnightGray)
      }
      if (agent.toAttack.isDefined) {
        DrawMap.arrow(unit.pixelCenter, agent.toAttack.get.pixelCenter, Colors.NeonYellow)
      }
      if (agent.toGather.isDefined) {
        DrawMap.arrow(unit.pixelCenter, agent.toGather.get.pixelCenter, Colors.MidnightGreen)
      }
    }
    if (showFormation) {
      if (agent.toForm.isDefined) {
        DrawMap.circle(agent.toForm.get, unit.unitClass.radialHypotenuse.toInt, Colors.MidnightTeal)
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
            unit.pixelCenter,
            unit.pixelCenter.add(
              forceNormalized.x.toInt,
              forceNormalized.y.toInt),
            pair._1)
        })
        if (agent.movingTo.isDefined) {
          DrawMap.arrow(
            unit.pixelCenter,
            agent.movingTo.get,
            ForceColors.sum)
        }
      }
    }
    
    if (showDesire) {
      val color = if (agent.shouldEngage) Colors.NeonGreen else Colors.NeonRed
      val pixel = unit.pixelCenter.subtract(0, 6 + unit.unitClass.height / 2)
      DrawMap.circle(pixel, 3, Color.Black, solid = true)
      DrawMap.circle(pixel, 2, color,       solid = true)
    }
  }
  
  def drawAttackCommand(attacker: UnitInfo, victim: UnitInfo) {
    DrawMap.circle(attacker.pixelCenter, attacker.unitClass.radialHypotenuse.toInt, Colors.BrightViolet)
  }
}
