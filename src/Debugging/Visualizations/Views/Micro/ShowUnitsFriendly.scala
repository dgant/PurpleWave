package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Debugging.Visualizations.{Colors, ForceColors}
import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.PixelRay
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption
import bwapi.Color

object ShowUnitsFriendly extends View {
  
  var selectedOnly    : Boolean = false
  var showClient      : Boolean = true
  var showAction      : Boolean = true
  var showCommand     : Boolean = true
  var showOrder       : Boolean = false
  var showTargets     : Boolean = true
  var showFormation   : Boolean = true
  var showPaths       : Boolean = true
  var showForces      : Boolean = true
  var showDesire      : Boolean = true
  var showDistance    : Boolean = false
  var showFightReason : Boolean = true

  override def renderMap() { With.units.ours.foreach(renderUnitState) }

  def renderUnitState(unit: FriendlyUnitInfo) {
    val agent = unit.agent
    if (selectedOnly && ! unit.selected) return
    if ( ! unit.aliveAndComplete && ! unit.unitClass.isBuilding) return
    if ( ! With.viewport.contains(unit.pixelCenter)) return
    if ( ! unit.unitClass.orderable) return
    if (unit.transport.isDefined) return

    var labelY = -28
    if (showFightReason) {
      DrawMap.label(
        unit.agent.fightReason,
        unit.pixelCenter.add(0, labelY),
        drawBackground = false)
      labelY += 7
    }
    if (showClient) {
      agent.lastClient.foreach(plan =>
        DrawMap.label(
          plan.toString,
          unit.pixelCenter.add(0, labelY),
          drawBackground = false))
      labelY += 7
    }
    if (showAction) {
      DrawMap.label(
        agent.lastAction.map(_.name).getOrElse(""),
        unit.pixelCenter.add(0, labelY),
        drawBackground = false)
      labelY += 7
    }
    if (showCommand) {
      DrawMap.label(
        unit.command.map(_.getUnitCommandType.toString).getOrElse(""),
        unit.pixelCenter.add(0, labelY),
        drawBackground = false)
      labelY += 7
    }
    if (showOrder) {
      DrawMap.label(
        unit.order.toString,
        unit.pixelCenter.add(0, labelY),
        drawBackground = false)
      labelY += 7
    }

    if (showTargets) {
      val targetUnit = unit.target.orElse(unit.orderTarget)
      if (targetUnit.nonEmpty) {
        DrawMap.line(unit.pixelCenter, targetUnit.get.pixelCenter, unit.player.colorNeon)
      }
      val targetPosition = unit.targetPixel.orElse(unit.orderTargetPixel)
      if (targetPosition.nonEmpty && unit.target.isEmpty) {
        DrawMap.line(unit.pixelCenter, targetPosition.get, unit.player.colorDark)
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
        DrawMap.box(
          agent.toForm.get.subtract (unit.unitClass.width / 2, unit.unitClass.height / 2),
          agent.toForm.get.add      (unit.unitClass.width / 2, unit.unitClass.height / 2),
        Colors.BrightViolet)
      }
    }

    if (showPaths) {
      def drawRayPath(ray: PixelRay, color: Color) {
        ray.tilesIntersected.foreach(tile => DrawMap.box(
          tile.topLeftPixel.add(1, 1),
          tile.bottomRightPixel.subtract(1, 1),
          if (With.grids.walkable.get(tile)) color else Colors.BrightRed))
      }
      def drawTilePath(path: TilePath): Unit = {
        for (i <- 0 until path.tiles.get.size - 1) {
          DrawMap.arrow(
            path.tiles.get(i).pixelCenter,
            path.tiles.get(i + 1).pixelCenter,
            Colors.White)
        }
      }
      agent.path.foreach(drawTilePath)
      agent.pathsAll.foreach(drawRayPath(_, Colors.BrightBlue))
      agent.pathsAcceptable.foreach(drawRayPath(_, Colors.BrightYellow))
    }
    
    if (showForces) {
      val length = 96.0
      val maxForce = ByOption.max(agent.forces.values.map(_.lengthSlow)).getOrElse(0.0)
      if (maxForce > 0.0) {
        (agent.forces ++ agent.resistances.flatMap(p => p._2.map(f => (p._1, f)))).foreach(pair => {
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

    if (showDistance) {
      DrawMap.arrow(unit.pixelCenter, agent.destination, Color.Black)
      DrawMap.label(
        unit.pixelDistanceTravelling(unit.agent.destination).toInt.toString,
        agent.unit.pixelCenter.add(0, 21),
        drawBackground = true,
        Color.Black)
    }
  }
  
  def drawAttackCommand(attacker: UnitInfo, victim: UnitInfo) {
    DrawMap.circle(attacker.pixelCenter, attacker.unitClass.radialHypotenuse.toInt, Colors.BrightViolet)
  }
}
