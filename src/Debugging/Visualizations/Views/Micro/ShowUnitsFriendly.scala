package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Debugging.Visualizations.{Colors, Forces}
import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption
import bwapi.Color

object ShowUnitsFriendly extends View {
  
  var selectedOnly    : Boolean = false
  var showClient      : Boolean = false
  var showAction      : Boolean = true
  var showCommand     : Boolean = false
  var showOrder       : Boolean = false
  var showTargets     : Boolean = true
  def showPaths       : Boolean = ShowUnitPaths.inUse
  var showDesire      : Boolean = true
  var showDistance    : Boolean = false
  var showFightReason : Boolean = true
  var showForces      : Boolean = true
  var showLeaders     : Boolean = true
  var showCharge      : Boolean = true
  
  override def renderMap() { With.units.ours.foreach(renderUnitState) }

  def renderTargets(unit: UnitInfo): Unit = {
    val targetUnit = unit.presumptiveTarget
    val targetPosition = unit.presumptiveDestination
    if (targetUnit.exists(_.unitClass.isResource)) {
      targetUnit.map(_.pixelCenter).foreach(DrawMap.line(unit.pixelCenter, _, Colors.MediumTeal))
    } else if (targetUnit.isDefined) {
      targetUnit.map(_.pixelCenter).foreach(DrawMap.line(unit.pixelCenter, _, Colors.MediumYellow))
    }
    if (targetPosition != unit.pixelCenter && unit.orderTarget.isEmpty) {
      DrawMap.line(unit.pixelCenter, targetPosition, Colors.MediumGray)
      renderUnitBoxAt(unit, targetPosition, Colors.MediumGray)
      unit.friendly.map(_.agent).foreach(agent => {
        agent.toReturn.foreach(toReturn => {
          val returnColor = Colors.MediumViolet
          if (targetPosition != toReturn) {
            DrawMap.line(targetPosition, toReturn, returnColor)
            renderUnitBoxAt(unit, agent.toReturn.get, returnColor)
          }
        })
      })
    }
  }

  def renderUnitBoxAt(unit: UnitInfo, at: Pixel, color: Color): Unit = {
    DrawMap.box(
      at.subtract (unit.unitClass.width / 2, unit.unitClass.height / 2),
      at.add      (unit.unitClass.width / 2, unit.unitClass.height / 2),
      color)
  }
  
  def renderUnitState(unit: FriendlyUnitInfo) {
    val agent = unit.agent
    if (selectedOnly && ! unit.selected) return
    if ( ! unit.aliveAndComplete && ! unit.unitClass.isBuilding) return
    if ( ! With.viewport.contains(unit.pixelCenter)) return
    if ( ! unit.unitClass.orderable) return
    if (unit.transport.isDefined) return
    
    var labelY = -28
    def drawNextLabel(value: String): Unit = {
      DrawMap.label(value, unit.pixelCenter.add(0, labelY), drawBackground = false)
      labelY += 7
    }

    if (showTargets) {
      renderTargets(unit)
    }

    if (showLeaders) {
      if (agent.leader().contains(unit)) {
        val start = unit.pixelCenter.add(0, unit.unitClass.dimensionDown + 8)
        DrawMap.circle(start, 5, color = unit.player.colorMidnight, solid = true)
        DrawMap.drawStar(start, 4, Colors.NeonYellow)
      }
    }

    if (showDesire && unit.battle.isDefined && (unit.canMove || unit.canAttack)) {
      val color = if (agent.shouldEngage) Colors.NeonGreen else Colors.NeonRed
      val pixel = unit.pixelCenter.subtract(0, 6 + unit.unitClass.height / 2)
      DrawMap.box(pixel.subtract(3, 3), pixel.add(3, 3), Color.Black, solid = true)
      DrawMap.box(pixel.subtract(2, 2), pixel.add(2, 2), color,       solid = true)
    }

    if (showCharge) {
      if (unit.unitClass.spells.exists(spell => spell.energyCost > 0 && With.self.hasTech(spell) && unit.energy >= spell.energyCost)) {
        val degrees = System.currentTimeMillis() % 360
        val radians = degrees * Math.PI / 360
        DrawMap.circle(unit.pixelCenter.radiateRadians(radians, 10), 2, Colors.NeonYellow, solid = true)
        DrawMap.circle(unit.pixelCenter.radiateRadians(radians, -10), 2, Colors.NeonYellow, solid = true)
      }
    }

    if (showPaths && (unit.selected || unit.transport.exists(_.selected) || With.units.selected().isEmpty)) {
      def drawTilePath(path: TilePath): Unit = {
        for (i <- 0 until path.tiles.get.size - 1) {
          DrawMap.arrow(
            path.tiles.get(i).pixelCenter,
            path.tiles.get(i + 1).pixelCenter,
            Colors.White)
        }
      }
      unit.agent.lastPath.foreach(drawTilePath)
    }

    if (showForces) {
      val forceLengthMax = 48.0
      val forceRadiusMin = 5
      val maxForce = ByOption.max(agent.forces.values.view.map(_.lengthSlow)).getOrElse(0.0)
      if (maxForce > 0.0) {
        DrawMap.circle(unit.pixelCenter, forceRadiusMin, Color.White)
        (agent.forces.view ++ Seq((Forces.sum, agent.forces.sum)))
          .filter(_._2.lengthSquared > 0)
          .foreach(pair => {
            val force           = pair._2
            val forceNormalized = force.normalize(Math.max(24, forceLengthMax * force.lengthSlow / maxForce))
            val to = unit.pixelCenter.add(
              forceNormalized.x.toInt,
              forceNormalized.y.toInt)
            DrawMap.arrow(unit.pixelCenter.project(to, 8), to, pair._1.color)
          })
        }
    }

    if (showFightReason)  drawNextLabel(if (unit.battle.isDefined) unit.agent.fightReason else "")
    if (showClient)       drawNextLabel(agent.lastClient.map(_.toString).getOrElse(""))
    if (showAction)       drawNextLabel(agent.lastAction.getOrElse(""))
    if (showCommand)      drawNextLabel(unit.command.map(_.getType.toString).getOrElse(""))
    if (showOrder)        drawNextLabel(unit.order.toString)

    if (showDistance) {
      DrawMap.arrow(unit.pixelCenter, agent.destination, Color.Black)
      DrawMap.label(
        unit.pixelDistanceTravelling(unit.agent.destination).toInt.toString,
        agent.unit.pixelCenter.add(0, 21),
        drawBackground = true,
        Color.Black)
    }
  }
}
