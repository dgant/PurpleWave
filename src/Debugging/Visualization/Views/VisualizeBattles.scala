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
    val ourColor      = With.self.colorDark
    val enemyColor    = With.enemies.head.colorDark
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
      Vector(formatStrength(battle.us.strength), formatStrength(battle.enemy.strength)),
      bottomRight,
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
  }
  
  private def drawBattleReport(battle:BattleSimulation) {
    
    val winner = if (battle.us.lostValue <= battle.enemy.lostValue) With.self else With.enemies.head
    With.game.drawTextScreen(new Position(5, 31), "Advantage: " + winner.name)
    drawPlayerReport(battle.us,     With.self.name,         new Position(5, 50))
    drawPlayerReport(battle.enemy,  With.enemies.head.name, new Position(130, 50))
  }
  
  private def drawPlayerReport(group: BattleSimulationGroup, name:String, origin:Position) {
    DrawScreen.table(
      origin.getX,
      origin.getY,
      Vector(
        Vector(name),
        Vector("Losses:",   group.lostValue.toString),
        Vector("Move:",     group.tactics.movement.toString),
        Vector("Focus:",    group.tactics.focusAirOrGround.toString),
        Vector("Workers:",  group.tactics.workers.toString),
        Vector("Wounded:",  group.tactics.wounded.toString),
        Vector(),
        Vector("Losses:")
      )
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
}
