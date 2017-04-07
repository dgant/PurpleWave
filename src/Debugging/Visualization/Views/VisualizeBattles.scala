package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Information.Battles.Battle
import Lifecycle.With
import Utilities.EnrichPosition._
import bwapi.Color

object VisualizeBattles {
  def render() = {
    With.game.drawTextScreen(438, 18, "Total strength:")
    With.game.drawTextScreen(521, 18, formatStrength(With.battles.global.us.strength))
    With.game.drawTextScreen(589, 18, formatStrength(With.battles.global.enemy.strength))
    With.battles.local.foreach(drawBattle)
  }
  
  def formatStrength(strength:Double):String = (strength/1000).toInt.toString
  
  private def drawBattle(battle:Battle) {
    val ourColor    = DrawMap.playerColorDark(With.self)
    val enemyColor  = DrawMap.playerColorDark(With.enemies.headOption.get)
    val neutralColor = Color.Black
    DrawMap.circle(battle.focus,          8, neutralColor)
    DrawMap.circle(battle.us.vanguard,    8, ourColor)
    DrawMap.circle(battle.enemy.vanguard, 8, enemyColor)
    DrawMap.line(battle.focus, battle.us.vanguard,    ourColor)
    DrawMap.line(battle.focus, battle.enemy.vanguard, enemyColor)
    val topLeft = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).minBound.subtract(16, 16)
    val bottomLeft = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).bottomLeftBound.add(16, 16)
    val bottomRight = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).maxBound.add(16, 16)
    val winnerStrengthColor = if (battle.us.strength >= battle.enemy.strength)  ourColor else enemyColor
    val winnerValueColor    = if (battle.ourLosses  <   battle.enemyLosses)     ourColor else enemyColor
    DrawMap.box(
      topLeft,
      bottomRight,
      neutralColor)
    DrawMap.labelBox(
      List(formatStrength(battle.us.strength), formatStrength(battle.enemy.strength)),
      bottomRight,
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
    DrawMap.labelBox(
      List(battle.ourLosses.toString, battle.enemyLosses.toString),
      bottomLeft,
      drawBackground = true,
      backgroundColor = winnerValueColor)
  }
}
