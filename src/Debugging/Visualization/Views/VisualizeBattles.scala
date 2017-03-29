package Debugging.Visualization.Views

import Debugging.Visualization.Colors
import Debugging.Visualization.Rendering.DrawMap
import Micro.Battles.Battle
import Startup.With
import Utilities.EnrichPosition._
import bwapi.Color

object VisualizeBattles {
  def render() = With.battles.allAdHoc.foreach(drawBattle)
  
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
      List(
        (battle.us.strength/100).toInt.toString,
        (battle.enemy.strength/100).toInt.toString
      ),
      battle.us.vanguard.add(0, 16),
      drawBackground = true,
      backgroundColor = neutralColor)
  }
}
