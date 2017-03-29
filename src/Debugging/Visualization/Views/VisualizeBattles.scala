package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Information.Battles.Battle
import Startup.With
import Utilities.EnrichPosition._
import bwapi.Color

object VisualizeBattles {
  def render() = {
    With.game.drawTextScreen(438, 18, "Total strength:")
    With.game.drawTextScreen(521, 18, formatStrength(With.battles.global.us.strength))
    With.game.drawTextScreen(589, 18, formatStrength(With.battles.global.enemy.strength))
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
    DrawMap.box(
      battle.us.units.map(_.pixelCenter).minBound,
      battle.us.units.map(_.pixelCenter).maxBound,
      ourColor)
    DrawMap.box(
      battle.enemy.units.map(_.pixelCenter).minBound,
      battle.enemy.units.map(_.pixelCenter).maxBound,
      enemyColor)
    DrawMap.labelBox(
      List(formatStrength(battle.us.strength), formatStrength(battle.enemy.strength)),
      battle.us.vanguard.add(0, 16),
      drawBackground = true,
      backgroundColor = neutralColor)
  }
}
