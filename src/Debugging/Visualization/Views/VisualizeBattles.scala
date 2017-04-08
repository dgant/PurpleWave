package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Information.Battles.Battle
import Information.Battles.Simulation.Construction.{BattleSimulation, BattleSimulationGroup}
import Lifecycle.With
import Utilities.EnrichPosition._
import bwapi.{Color, Position}

object VisualizeBattles {
  def render() = {
    With.game.drawTextScreen(438, 18, "Total strength:")
    With.game.drawTextScreen(521, 18, formatStrength(With.battles.global.us.strength))
    With.game.drawTextScreen(589, 18, formatStrength(With.battles.global.enemy.strength))
    With.battles.local.foreach(drawBattle)
    
    With.battles.local
      .filter(battle => battle.simulations.nonEmpty && With.viewport.contains(battle.focus))
      .flatten(_.bestSimulationResult)
      .foreach(drawBattleReport)
  }
  
  def formatStrength(strength:Double):String = (strength/1000).toInt.toString
  
  private def drawBattle(battle:Battle) {
    val ourColor    = DrawMap.playerColorDark(With.self)
    val enemyColor  = DrawMap.playerColorDark(With.enemies.head)
    val neutralColor = Color.Black
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
    With.game.drawBoxScreen(new Position(0, 25), new Position(150, 50), DrawMap.playerColorDark(winner))
    With.game.drawTextScreen(new Position(50, 31), "Advantage: " + winner.getName)
    With.game.drawBoxScreen(new Position(0, 50), new Position(75, 125), DrawMap.playerColorDark(With.self))
    With.game.drawBoxScreen(new Position(75, 50), new Position(150, 125), DrawMap.playerColorDark(With.enemies.head))
    drawPlayerReport(battle.us,     new Position(0, 50))
    drawPlayerReport(battle.enemy,  new Position(75, 50))
  }
  
  private def drawPlayerReport(group: BattleSimulationGroup, origin:Position) {
    With.game.drawTextScreen(
      origin,
      List(
        "Losses:  " + group.lostValue,
        "Flee?    " + group.strategy.fleeWounded.toString,
        "Focus?   " + group.strategy.focusAirOrGround,
        "Move?    " + group.strategy.movement,
        "Workers? " + group.strategy.workersFighting
      ).mkString("\n"))
  }
}
