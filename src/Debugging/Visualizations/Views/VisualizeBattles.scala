package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Information.Battles.BattleTypes.Battle
import Information.Battles.Estimation.BattleEstimation
import Information.Battles.Simulation.Construction.{BattleSimulation, BattleSimulationGroup}
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import Mathematics.Pixels.Pixel
import Planning.Yolo
import Utilities.EnrichPixel._
import bwapi.Color

object VisualizeBattles {
  def render() = {
    With.game.drawTextScreen(438, 18, "Total strength:")
    With.game.drawTextScreen(521, 18, formatStrength(With.battles.global.us.strength))
    With.game.drawTextScreen(589, 18, formatStrength(With.battles.global.enemy.strength))
    With.battles.local.foreach(drawBattle)
    val localBattles = With.battles.local.filter(_.happening)
    if (localBattles.nonEmpty) {
      val battle = localBattles.minBy(battle => battle.focus.pixelDistanceSquared(With.viewport.center))
      val tactics = battle.bestTactics
      val simulation = battle.simulation(tactics)
      val estimation = battle.estimation(tactics)
      With.game.drawTextScreen(5,   50, With.self.name)
      With.game.drawTextScreen(130, 50, With.enemies.head.name)
      simulation.foreach(drawSimulationReport)
      estimation.foreach(drawEstimationReport)
      drawTacticsReport(battle)
    }
    if (Yolo.enabled && With.frame / 24 % 2 == 0) {
      With.game.drawTextScreen(5, 5, "YOLO")
    }
  }
  
  def formatStrength(strength:Double):String = (strength/1000).toInt.toString
  
  private def drawBattle(battle:Battle) {
    val ourColor      = With.self.colorDark
    val enemyColor    = With.enemies.head.colorDark
    val neutralColor  = Color.Black
    DrawMap.circle(battle.focus,          8, neutralColor)
    DrawMap.circle(battle.us.vanguard,    8, ourColor)
    DrawMap.circle(battle.enemy.vanguard, 8, enemyColor)
    DrawMap.line(battle.focus, battle.us.vanguard,    ourColor)
    DrawMap.line(battle.focus, battle.enemy.vanguard, enemyColor)
    val topLeft       = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).minBound.subtract(16, 16)
    val bottomRight   = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).maxBound.add(16, 16)
    val winnerStrengthColor = if (battle.us.strength >= battle.enemy.strength) ourColor else enemyColor
    DrawMap.box(
      topLeft,
      bottomRight,
      neutralColor)
    DrawMap.labelBox(
      Vector(formatStrength(battle.us.strength), formatStrength(battle.enemy.strength)),
      battle.focus.add(24, 0),
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
  }
  
  private def drawSimulationReport(simulation:BattleSimulation) {
    val winner = if (simulation.us.lostValue <= simulation.enemy.lostValue) With.self else With.enemies.head
    With.game.drawTextScreen(Pixel(5, 31).bwapi, "Advantage: " + winner.name)
    drawPlayerReport(simulation.us,     With.self.name,         Pixel(5,   130))
    drawPlayerReport(simulation.enemy,  With.enemies.head.name, Pixel(130, 130))
    simulation.events.foreach(_.draw())
  }
  
  private def drawEstimationReport(estimation:BattleEstimation) {
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.game.drawTextScreen(255, 50, "+" + 10 * estimation.damageToEnemy.toInt)
    With.game.drawTextScreen(255, 75, "-" + 10 * estimation.damageToUs.toInt)
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
  
  private def drawPlayerReport(group: BattleSimulationGroup, name:String, origin:Pixel) {
    DrawScreen.table(
      origin.x,
      origin.y,
      Vector(Vector("Losses:"))
      ++ group.lostUnits
        .groupBy(_.unit.unitClass)
        .toVector
        .sortBy(_._1.toString)
        .map(u => Vector(u._1.toString, u._2.size.toString))
      ++ Vector(Vector.empty)
      ++ Vector(Vector("Survivors:"))
      ++ group.units
        .groupBy(_.unit.unitClass)
        .toVector
        .sortBy(_._1.toString)
        .map(u => Vector(u._2.size.toString, u._1.toString)))
  }
  
  private def getMove(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Movement.Charge))  return "Charge"
    if (tactics.has(Tactics.Movement.Kite))    return "Kite"
    if (tactics.has(Tactics.Movement.Flee))    return "Flee"
    return "-"
  }
  
  private def getFocus(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Focus.Air))    return "Air"
    if (tactics.has(Tactics.Focus.Ground)) return "Ground"
    return "-"
  }
  
  private def getWounded(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Wounded.Flee))  return "Flee"
    return "-"
  }
  
  private def getWorkers(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Workers.FightAll))   return "Fight (All)"
    if (tactics.has(Tactics.Workers.FightHalf))  return "Fight (Half)"
    if (tactics.has(Tactics.Workers.Flee))       return "Flee"
    return "-"
  }
  
  private def drawTacticsReport(battle:Battle) {
  
    DrawScreen.table(
      5,
      76,
      Vector(
        Vector("Move:",     getMove(battle.bestTactics)),
        Vector("Focus:",    getFocus(battle.bestTactics)),
        Vector("Workers:",  getWorkers(battle.bestTactics)),
        Vector("Wounded:",  getWounded(battle.bestTactics))
      ))
    
    val heuristicsTable = Vector(Vector("Value", "Tactics")) ++
      battle.tacticsHeuristicResults.map(r => Vector(
        "%.2f".format(r.evaluation),
        r.heuristic.getClass.getSimpleName.replace("TacticsHeuristic", "")))
    //DrawScreen.table(300, 50, heuristicsTable)
    
    With.game.drawTextScreen(300, 50, battle.rankedTactics.map(tactic => tactic.toString).mkString("\n"))
  }
  
  
  private def drawTacticsReport(tactics: TacticsOptions, origin:Pixel) {
    DrawScreen.table(
      origin.x,
      origin.y,
      Vector(
        Vector("Move:",     getMove(tactics)),
        Vector("Focus:",    getFocus(tactics)),
        Vector("Workers:",  getWorkers(battle.bestTactics)),
        Vector("Wounded:",  getWounded(battle.bestTactics))
      ))
  }
}
