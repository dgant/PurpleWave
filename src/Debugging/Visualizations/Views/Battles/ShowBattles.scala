package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

object ShowBattles extends View {
  
  override def renderScreen() {
    //localBattle.foreach(renderBattleScreen)

    if (With.yolo.active() && With.frame / 24 % 2 == 0) {
      DrawScreen.column(610, 230, "YOLO")
    }
  }

  override def renderMap(): Unit = {
    //localBattle.foreach(renderBattleMap)
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

  /*
  def renderBattleScreen(battle: BattleLocal) {
    val prediction = battle.predictionAttack
    val barHeight = With.visualization.lineHeightSmall
    val x = 5

    prediction.localBattleMetrics.lastOption.foreach(metrics => {
      DrawScreen.table(x, 4 * barHeight, Vector(
        Vector("",            "",                                           "Survive:"),
        Vector("Snipe",       format(battle.judgement.get.ratioAttack),     With.self.name,   describeTeam(prediction.debugReport.filter(_._1.isFriendly) .filterNot(_._2.dead).keys)),
        Vector("Target",      format(battle.judgement.get.ratioThreshold),  With.enemy.name,  describeTeam(prediction.debugReport.filter(_._1.isEnemy)    .filterNot(_._2.dead).keys)),
        Vector("Hysteresis",  format(battle.judgement.get.hysteresis)),
        Vector("Turtle",      format(battle.judgement.get.turtleBonus),     "Die:"),
        Vector("Hornet",      format(battle.judgement.get.hornetBonus),     With.self.name,   describeTeam(prediction.debugReport.filter(_._1.isFriendly) .filter(_._2.dead).keys)),
        Vector("Siege",       format(battle.judgement.get.siegeUrgency),    With.enemy.name,  describeTeam(prediction.debugReport.filter(_._1.isEnemy)    .filter(_._2.dead).keys)),
        Vector("LVLR",        format(metrics.localValueLostRatio)),
        Vector("LHLR",        format(metrics.localHealthLostRatio),         "Duration",       metrics.framesIn / 24 + "s"),
        Vector("LHVLR",       format(metrics.localHealthValueLostRatio),    "Metrics",        prediction.localBattleMetrics.size.toString),
        Vector("RLVLN",       format(metrics.ratioLocalValueLostNet)),
        Vector("RLHLN",       format(metrics.ratioLocalHealthLostNet)),
        Vector("RLHVLN",      format(metrics.ratioLocalHealthValueLostNet)),
        Vector("Confidence",  format(battle.judgement.get.confidence))
      ))
    })

    val graphWidth = 82
    DrawScreen.graph(
      Pixel(1, 218),
      "Score",
      Seq(
        GraphCurve(Color.Black,         prediction.localBattleMetrics.map(unused =>  1.0)),
        GraphCurve(Color.Black,         prediction.localBattleMetrics.map(unused =>  0.0)),
        GraphCurve(Color.Black,         prediction.localBattleMetrics.map(unused => -1.0)),
        GraphCurve(Colors.MediumRed,    prediction.localBattleMetrics.map(unused => battle.judgement.get.ratioThreshold)),
        GraphCurve(Colors.BrightOrange, prediction.localBattleMetrics.map(unused => battle.judgement.get.ratioAttack)),
        GraphCurve(Color.Yellow,        prediction.localBattleMetrics.map(_.totalScore))),
      fixedYMin = Some(-1.0),
      fixedYMax = Some(1.0),
      width = graphWidth,
      height = graphWidth)
  }

  def renderBattleMap(battle: BattleLocal) {
    val simulation = battle.predictionAttack.simulation
    simulation.simulacra.values.foreach(renderSimulacrumMap(_, battle.judgement.get.shouldFight))
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
  */
}

