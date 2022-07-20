package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Debugging.Visualizations.{Colors, Forces}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Color

object ShowUnitsFriendly extends DebugView {
  
  var selectedOnly    : Boolean = false
  var showClient      : Boolean = false
  var showAction      : Boolean = true
  var showOrder       : Boolean = true
  def showPaths       : Boolean = ShowUnitPaths.inUse
  var showDesire      : Boolean = true
  var showDistance    : Boolean = false
  var showFightReason : Boolean = true
  var showForces      : Boolean = true
  var showLeaders     : Boolean = true
  var showCharge      : Boolean = true
  var showTarget      : Boolean = true
  
  override def renderMap(): Unit = { With.units.ours.foreach(renderUnitState) }

  def renderUnitBoxAt(unit: UnitInfo, at: Pixel, color: Color): Unit = {
    DrawMap.box(
      at.subtract (unit.unitClass.width / 2, unit.unitClass.height / 2),
      at.add      (unit.unitClass.width / 2, unit.unitClass.height / 2),
      color)
  }
  
  def renderUnitState(unit: FriendlyUnitInfo): Unit = {
    val agent = unit.agent

    var marker: Option[String] = None
    var origin = unit.pixel
    unit.transport.foreach(transport => {
      marker = Some(f"Loaded ${unit.unitClass}")
      val index = transport.loadedUnits.zipWithIndex.find(p => p._1 == unit).map(_._2).getOrElse(0)
      origin = unit.pixel.subtract(0, 32 * (1 + index))
    })
    if ( ! unit.complete && ! unit.morphing && ! unit.unitClass.isBuilding) {
      marker = Some(f"Training ${unit.unitClass}")
      origin.add(0, 16)
    }
    
    if (selectedOnly && ! unit.selected && ! unit.transport.exists(_.selected)) return
    if ( ! With.viewport.contains(origin)) return
    if ( ! unit.unitClass.orderable) return

    var labelY = -28
    
    def drawNextLabel(value: String): Unit = {
      DrawMap.label(value, origin.add(0, labelY), drawBackground = false)
      labelY += 7
    }

    marker.foreach(drawNextLabel)

    if (showLeaders) {
      if (agent.leader().contains(unit)) {
        val start = origin.add(0, unit.unitClass.dimensionDown + 8)
        DrawMap.circle(start, 5, color = unit.player.colorMidnight, solid = true)
        DrawMap.star(start, 4, Colors.NeonYellow)
      }
    }

    if (showDesire && unit.battle.isDefined && (unit.canMove || unit.canAttack)) {
      val color = if (agent.shouldEngage) Colors.NeonGreen else Colors.NeonRed
      val pixel = origin.subtract(0, 6 + unit.unitClass.height / 2)
      DrawMap.box(pixel.subtract(4, 4), pixel.add(4, 4), Color.Black, solid = true)
      DrawMap.box(pixel.subtract(3, 3), pixel.add(3, 3), color,       solid = true)
    }

    if (showCharge) {
      if (unit.unitClass.spells.exists(spell => spell() && spell.energyCost > 0 && unit.energy >= spell.energyCost)) {
        val degrees = System.currentTimeMillis() % 360
        val radians = degrees * Math.PI / 360
        DrawMap.circle(origin.radiateRadians(radians, 10), 2, Colors.NeonYellow, solid = true)
        DrawMap.circle(origin.radiateRadians(radians, -10), 2, Colors.NeonYellow, solid = true)
      }
    }

    if (showPaths && (unit.selected || unit.transport.exists(_.selected) || With.units.selected.isEmpty)) {
      unit.agent.lastPath.foreach(_.renderMap(unit.unitColor, Some(origin)))
    }

    if (showForces) {
      val forceLengthMax = unit.unitClass.radialHypotenuse + 16.0
      val forceRadiusMin = 4
      val maxForce = Maff.max(agent.forces.values.view.map(_.lengthSlow)).getOrElse(0.0)
      if (maxForce > 0.0) {
        DrawMap.circle(origin, forceRadiusMin, Color.White)
        (agent.forces.view ++ Seq((Forces.sum, agent.forces.sum)))
          .filter(_._2.lengthSquared > 0)
          .foreach(pair => {
            val force           = pair._2
            val forceNormalized = force.normalize(Math.max(24, forceLengthMax * force.lengthSlow / maxForce))
            val to = origin.add(
              forceNormalized.x.toInt,
              forceNormalized.y.toInt)
            DrawMap.arrow(origin.project(to, 8), to, pair._1.color)
          })
        }
    }

    if (showFightReason)  drawNextLabel(if (unit.battle.isDefined) unit.agent.fightReason else "")
    if (showClient)       drawNextLabel(unit.friendly.map(_.client.toString).getOrElse(""))
    if (showAction)       drawNextLabel(agent.lastAction.getOrElse(""))
    if (showOrder)        drawNextLabel(unit.order.toString)

    if (showDistance) {
      DrawMap.arrow(origin, agent.destination, Color.Black)
      DrawMap.label(
        unit.pixelDistanceTravelling(unit.agent.destination).toInt.toString,
        unit.pixel.add(0, 21),
        drawBackground = true,
        Color.Black)
    }

    if (showTarget) {
      unit.orderTarget.foreach(t => {
        DrawMap.box(t.topLeft, t.bottomRight, unit.unitColor)
        DrawMap.line(unit.pixel, t.pixel, unit.unitColor)
      })
      unit.orderTargetPixel.filter(p => p.x > 0 && p.y > 0).foreach(t => {
        val tp = if (t.pixelDistance(unit.pixel) < 16) t else t.project(unit.pixel, 2)
        DrawMap.line(tp, unit.pixel, unit.unitColor)
        DrawMap.circle(t, 2, unit.unitColor)
      })
    }
  }
}
