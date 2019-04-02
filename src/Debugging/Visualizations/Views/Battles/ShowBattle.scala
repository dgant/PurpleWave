package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawScreen.GraphCurve
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Information.Battles.Prediction.Prediction
import Information.Battles.Prediction.Simulation.Simulacrum
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption
import bwapi.Color

object ShowBattle extends View {
  
  override def renderScreen() {
    localBattle.foreach(battle => renderBattleScreen(battle, battle.estimationSimulationAttack))

    if (With.yolo.active() && With.frame / 24 % 2 == 0) {
      DrawScreen.column(610, 230, "YOLO")
    }
  }

  override def renderMap(): Unit = {
    localBattle.foreach(renderBattleMap)
  }

  def localBattle: Option[BattleLocal] = {
    val battlesSelected = With.units.ours.filter(_.selected).flatMap(_.battle)
    val battlesToShow = if (battlesSelected.isEmpty) With.battles.local else battlesSelected
    ByOption.minBy(battlesToShow)(_.focus.pixelDistanceSquared(With.viewport.center))
  }

  def format(value: Double): String = "%1.2f".format(value)

  def describeTeam(units: Iterable[UnitInfo]): String = {
    units.groupBy(_.unitClass).toVector.sortBy( - _._2.size).map(p => p._2.size + " " + p._1).mkString(", ")
  }

  def renderBattleScreen(battle: BattleLocal, estimation: Prediction) {
    val barHeight = With.visualization.lineHeightSmall
    val x = 5

    battle.estimationSimulationAttack.localBattleMetrics.lastOption.foreach(metrics => {
      DrawScreen.table(x, 4 * barHeight, Vector(
        Vector("Attack",      format(battle.ratioAttack)),
        Vector("Snipe",       format(battle.ratioSnipe)),
        Vector("Target",      format(battle.ratioTarget)),
        Vector("Hysteresis",  format(battle.hysteresis)),
        Vector("Trappedness", format(battle.trappedness)),
        Vector("Turtle",      format(battle.turtleBonus)),
        Vector("Turtle",      format(battle.urgency)),
        Vector("LVLR",        format(metrics.localValueLostRatio)),
        Vector("LHLR",        format(metrics.localHealthLostRatio)),
        Vector("LHVLR",       format(metrics.localHealthValueLostRatio)),
        Vector("RLVLN",       format(metrics.ratioLocalValueLostNet)),
        Vector("RLHLN",       format(metrics.ratioLocalHealthLostNet)),
        Vector("RLHVLN",      format(metrics.ratioLocalHealthValueLostNet)),
        Vector("Duration",    metrics.framesIn / 24 + "s"),
        Vector("Metrics",     battle.estimationSimulationAttack.localBattleMetrics.size.toString),
        Vector("Survivors"),
        Vector(With.self.name,  describeTeam(estimation.reportCards.filter(_._1.isFriendly) .filterNot(_._2.dead).keys)),
        Vector(With.enemy.name, describeTeam(estimation.reportCards.filter(_._1.isEnemy)    .filterNot(_._2.dead).keys)),
        Vector("Dead"),
        Vector(With.self.name,  describeTeam(estimation.reportCards.filter(_._1.isFriendly) .filter(_._2.dead).keys)),
        Vector(With.enemy.name, describeTeam(estimation.reportCards.filter(_._1.isEnemy)    .filter(_._2.dead).keys))
      ))
    })

    val graphWidth = 96
    DrawScreen.graph(
      Pixel(320 - graphWidth / 2, 360 - graphWidth),
      "Score",
      Seq(
        GraphCurve(Color.Black,         battle.estimationSimulationAttack.localBattleMetrics.map(unused =>  1.0)),
        GraphCurve(Color.Black,         battle.estimationSimulationAttack.localBattleMetrics.map(unused =>  0.0)),
        GraphCurve(Color.Black,         battle.estimationSimulationAttack.localBattleMetrics.map(unused => -1.0)),
        GraphCurve(Colors.MediumRed,    battle.estimationSimulationAttack.localBattleMetrics.map(unused => battle.ratioTarget)),
        GraphCurve(Colors.BrightOrange, battle.estimationSimulationAttack.localBattleMetrics.map(unused => battle.ratioAttack)),
        GraphCurve(Color.Yellow,        battle.estimationSimulationAttack.localBattleMetrics.map(_.totalScore))),
      fixedYMin = Some(-1.0),
      fixedYMax = Some(1.0),
      width = graphWidth,
      height = graphWidth)
  }

  def renderBattleMap(battle: BattleLocal) {
    val simulation = battle.estimationSimulationAttack.simulation.get
    simulation.simulacra.values.foreach(renderSimulacrumMap(_, battle.shouldFight))
  }
  
  def renderSimulacrumMap(sim: Simulacrum, shouldFight: Boolean) {
    val color = if (sim.isFriendly == shouldFight) sim.realUnit.player.colorNeon else sim.realUnit.player.colorMedium

    val distanceTotal = sim.events.map(e => e.from.pixelDistance(e.to)).sum
    val distanceToTraverse = distanceTotal / (With.frame % 8)
    var pixelAnimation = sim.pixelInitial
    var distanceTraveled = 0.0
    sim.events.foreach(event => {
      val distanceRemaining = distanceToTraverse - distanceTraveled
      if (distanceRemaining > 0) {
        val distanceToTravel = Math.min(distanceRemaining, event.from.pixelDistance(event.to))
        pixelAnimation = event.from.project(event.to, distanceToTravel)
        distanceTraveled += distanceToTravel
      }
    })

    sim.events.foreach(_.draw())
    DrawMap.circle(pixelAnimation, 3, solid = true, color = color)

    if (sim.dead) {
      val dark = sim.realUnit.player.colorMidnight
      val skullColor = sim.realUnit.player.colorBright
      DrawMap.box(sim.pixel.add(-3, 0), sim.pixel.add(4, 8), dark, solid = false)
      DrawMap.circle(sim.pixel, 5, skullColor,  solid = true)
      DrawMap.circle(sim.pixel, 5, dark,        solid = false)
      DrawMap.box(sim.pixel.add(-2,  1), sim.pixel.add(2, 7), skullColor,  solid = true)
      DrawMap.box(sim.pixel.add(-2, -2), sim.pixel.add(0, 0), Color.Black, solid = true)
      DrawMap.box(sim.pixel.add(1,  -2), sim.pixel.add(3, 0), Color.Black, solid = true)
      DrawMap.line(sim.pixel.add(-1, 3), sim.pixel.add(-2, 9), dark)
      DrawMap.line(sim.pixel.add(1,  3), sim.pixel.add(2,  9), dark)
    }
  }
}

