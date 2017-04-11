package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.{DrawMap, DrawScreen}
import Information.Battles.Battle
import Information.Battles.Simulation.Construction.{BattleSimulation, BattleSimulationGroup}
import Lifecycle.With
import Planning.Yolo
import Utilities.EnrichPosition._
import bwapi.{Color, Position}

object VisualizeBattles {
  def render() = {
    With.game.drawTextScreen(438, 18, "Total strength:")
    With.game.drawTextScreen(521, 18, formatStrength(With.battles.global.us.strength))
    With.game.drawTextScreen(589, 18, formatStrength(With.battles.global.enemy.strength))
    With.battles.local.foreach(drawBattle)
    val battlesWithSimulations = With.battles.local.filter(_.simulations.nonEmpty)
    if (battlesWithSimulations.nonEmpty) {
      
      drawBattleReport(
        With.battles.local
          .minBy(battle => battle.focus.pixelDistanceSquared(With.viewport.center))
          .bestSimulationResult
          .get)
    }
    if (Yolo.enabled && With.frame / 24 % 2 == 0) {
      With.game.drawTextScreen(5, 5, "YOLO")
    }
  }
  
  def formatStrength(strength:Double):String = (strength/1000).toInt.toString
  
  private def drawBattle(battle:Battle) {
    val ourColor      = DrawMap.playerColorDark(With.self)
    val enemyColor    = DrawMap.playerColorDark(With.enemies.head)
    val neutralColor  = Color.Black
    DrawMap.circle(battle.focus,          8, neutralColor)
    DrawMap.circle(battle.us.vanguard,    8, ourColor)
    DrawMap.circle(battle.enemy.vanguard, 8, enemyColor)
    DrawMap.line(battle.focus, battle.us.vanguard,    ourColor)
    DrawMap.line(battle.focus, battle.enemy.vanguard, enemyColor)
    val topLeft     = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).minBound.subtract(16, 16)
    val bottomLeft  = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).bottomLeftBound.add(-16, 16)
    val bottomRight = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).maxBound.add(16, 16)
    val winnerStrengthColor = if (battle.us.strength >= battle.enemy.strength) ourColor else enemyColor
    DrawMap.box(
      topLeft,
      bottomRight,
      neutralColor)
    DrawMap.labelBox(
      List(formatStrength(battle.us.strength), formatStrength(battle.enemy.strength)),
      bottomRight,
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
  }
  
  private def drawBattleReport(battle:BattleSimulation) {
    
    val winner = if (battle.us.lostValue <= battle.enemy.lostValue) With.self else With.enemies.head
    With.game.drawTextScreen(new Position(5, 31), "Advantage: " + winner.getName)
    drawPlayerReport(battle.us,     With.self.getName,          new Position(5, 50))
    drawPlayerReport(battle.enemy,  With.enemies.head.getName,  new Position(130, 50))
  }
  
  private def drawPlayerReport(group: BattleSimulationGroup, name:String, origin:Position) {
    DrawScreen.table(
      origin.getX,
      origin.getY,
      List(
        List(name),
        List("Losses:",   group.lostValue.toString),
        List("Move:",     group.tactics.movement.toString),
        List("Focus:",    group.tactics.focusAirOrGround.toString),
        List("Workers:",  group.tactics.workers.toString),
        List("Wounded:",  group.tactics.wounded.toString),
        List(),
        List("Losses:")
      )
      ++ group.lostUnits
        .groupBy(_.unit.unitClass)
        .toList
        .sortBy(_._1.toString)
        .map(u => List(u._1.toString, u._2.size.toString))
      ++ List(List.empty)
      ++ List(List("Survivors:"))
      ++ group.units
        .groupBy(_.unit.unitClass)
        .toList
        .sortBy(_._1.toString)
        .map(u => List(u._1.toString, u._2.size.toString)))
  }
}
