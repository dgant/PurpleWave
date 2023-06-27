package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawScreen.GraphCurve
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.DebugView
import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowBattles extends DebugView {
  
  override def renderScreen(): Unit = {
    localBattle.foreach(renderBattleScreen)
    if (With.yolo.active && With.frame / 24 % 2 == 0) {
      DrawScreen.column(610, 230, "YOLO")
    }
  }

  override def renderMap(): Unit = {
    localBattle.foreach(renderBattleMap)
  }

  def localBattle: Option[Battle] = {
    val battlesSelected = With.units.ours.filter(_.selected).flatMap(_.battle)
    val battlesToShow = if (battlesSelected.isEmpty) With.battles.local else battlesSelected
    Maff.minBy(battlesToShow)(_.focus.pixelDistanceSquared(With.viewport.center))
  }

  def format(value: Double): String = "%1.2f".format(value)

  def describeTeam(units: Iterable[UnitInfo]): String = {
    units.groupBy(_.unitClass).toVector.sortBy( - _._2.size).map(p => f"${p._2.size} ${p._1.toString.take(4)}").mkString(", ")
  }

  def renderSkimulationScreen(battle: Battle): Unit = {
    val x = 5
    val us = battle.us
    val enemy = battle.enemy
    val judge = battle.judgement.get
    DrawScreen.table(x, 17 * With.visualization.lineHeightSmall, Vector(
      Vector("",          "Us",                            "Enemy",                             "Score",                    "Target",                   "Fight?"),
      Vector("Total",     format(us.skimStrengthTotal),    format(enemy.skimStrengthTotal),     format(judge.scoreTotal),   format(judge.scoreTarget),  if (judge.shouldFight)        "YES" else ""),
      Vector("Ground",    format(us.skimStrengthGround),   format(enemy.skimStrengthGround)     format(judge.scoreGround),  format(judge.scoreTarget),  if (judge.shouldFightGround)  "YES" else ""),
      Vector("Air",       format(us.skimStrengthAir),      format(enemy.skimStrengthAir)        format(judge.scoreAir),     format(judge.scoreTarget),  if (judge.shouldFightAir)     "YES" else ""),
      Vector("Vs Ground", format(us.skimStrengthVsGround), format(enemy.skimStrengthVsGround)),
      Vector("Vs Air",    format(us.skimStrengthVsAir),    format(enemy.skimStrengthVsAir))
    ))
  }

  def renderSimulationScreen(battle: Battle): Unit = {
    val x = 5
    battle.simulationCheckpoints.lastOption.foreach(metrics => {
      DrawScreen.table(x, 4 * With.visualization.lineHeightSmall, Vector(
        Vector("Score",       format(battle.judgement.get.scoreTotal),        "Our survivors:", describeTeam(battle.simulationReport.filter(_._1.isFriendly)  .filter(_._2.alive).keys)),
        Vector("Target",      format(battle.judgement.get.scoreTarget),       "Foe survivors:", describeTeam(battle.simulationReport.filter(_._1.isEnemy)     .filter(_._2.alive).keys)),
        Vector("LVLR",        format(metrics.localValueLostRatio)),
        Vector("LHLR",        format(metrics.localHealthLostRatio),           "Our deaths:",    describeTeam(battle.simulationReport.filter(_._1.isFriendly) .filterNot(_._2.alive).keys)),
        Vector("LHVLR",       format(metrics.localHealthValueLostRatio),      "Foe deaths:",    describeTeam(battle.simulationReport.filter(_._1.isEnemy)    .filterNot(_._2.alive).keys)),
        Vector("RLVLN",       format(metrics.ratioLocalValueLostNet)),
        Vector("RLHLN",       format(metrics.ratioLocalHealthLostNet),        f"${metrics.framesIn / 24} second duration"),
        Vector("RLHVLN",      format(metrics.ratioLocalHealthValueLostNet),   f"${battle.simulationCheckpoints.size} metrics checkpoints"),
        Vector("Confidence",  format(battle.judgement.get.confidence11Total))))
    })

    val graphWidth = 82
    DrawScreen.graph(
      Pixel(1, 226),
      "Score",
      Seq(
        GraphCurve(Color.Black,         battle.simulationCheckpoints.map(unused =>  1.0)),
        GraphCurve(Color.Black,         battle.simulationCheckpoints.map(unused =>  0.0)),
        GraphCurve(Color.Black,         battle.simulationCheckpoints.map(unused => -1.0)),
        GraphCurve(Colors.MediumRed,    battle.simulationCheckpoints.map(unused => battle.judgement.get.scoreTarget)),
        GraphCurve(Colors.BrightOrange, battle.simulationCheckpoints.map(unused => battle.judgement.get.scoreTotal)),
        GraphCurve(Color.Yellow,        battle.simulationCheckpoints.map(_.totalScore))),
      fixedYMin = Some(-1.0),
      fixedYMax = Some(1.0),
      width = graphWidth,
      height = graphWidth)
  }

  def renderBattleScreen(battle: Battle): Unit = {
    val skimBarWidth = 80
    val skimBarX0 = (640 - skimBarWidth) / 2
    val skimBarX2 = (640 + skimBarWidth) / 2
    val skimBarX1 = skimBarX0 + (skimBarWidth * battle.skimWeight).toInt
    val skimBarY0 = 340
    val skimBarY1 = skimBarY0 + 12
    With.game.drawBoxScreen(skimBarX0, skimBarY0, skimBarX1, skimBarY1, Color.Black, true)
    With.game.drawBoxScreen(skimBarX1, skimBarY0, skimBarX2, skimBarY1, Color.White, true)
    With.game.drawTextScreen(skimBarX0 - 19, skimBarY0 + 1, "Skim")
    With.game.drawTextScreen(skimBarX2 + 1, skimBarY0 + 1, "Sim")
    if (battle.skimulated) renderSkimulationScreen(battle)
    if (battle.simulated) renderSimulationScreen(battle)
  }

  def renderSkimulationMap(battle: Battle): Unit = {
    battle.units.foreach(u => DrawMap.labelDot(
      format(u.skimStrengthDisplay),
      u.pixel,
      Colors.hsv(u.player.hue, 255, Maff.clamp(64 + (128 * u.skimPresence).toInt, 64, 192))))
  }

  def renderBattleMap(battle: Battle): Unit = {
    if (battle.skimulated) {
      renderSkimulationMap(battle)
    }
    if ( ! battle.simulated) return

    val shouldFight = battle.judgement.get.shouldFight
    battle.units.view.filter(_.simulacrum.initialized).foreach(unit => {
      val sim = unit.simulacrum
      val color = if (sim.player.isUs == shouldFight) sim.player.colorNeon else sim.player.colorDark
      val distanceTotal = sim.events.view.map(e => e.from.pixelDistance(e.to)).sum
      val distanceToTraverse = distanceTotal / (With.frame % 8)
      var pixelAnimation = sim.realUnit.pixel
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
    })
  }
}

