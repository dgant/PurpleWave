package Debugging.Visualization

import Micro.Battles.Battle
import Startup.With
import bwapi.Color
import Utilities.EnrichPosition._

object VisualizeBattles {
  def render = With.battles.all.foreach(drawBattle)
  
  private def drawBattle(battle:Battle) {
    DrawMap.circle(battle.focus, 8, Color.Brown)
    DrawMap.circle(battle.us.vanguard, 8, Color.Blue)
    DrawMap.circle(battle.enemy.vanguard, 8, Color.Red)
    DrawMap.line(battle.focus, battle.us.vanguard, Color.Blue)
    DrawMap.line(battle.focus, battle.enemy.vanguard, Color.Red)
    DrawMap.box(
      battle.us.units.map(_.pixelCenter).minBound,
      battle.us.units.map(_.pixelCenter).maxBound,
      Color.Blue)
    DrawMap.box(
      battle.enemy.units.map(_.pixelCenter).minBound,
      battle.enemy.units.map(_.pixelCenter).maxBound,
      Color.Red)
    DrawMap.labelBox(
      List(
        (battle.us.strength/100).toInt.toString,
        (battle.enemy.strength/100).toInt.toString
      ),
      battle.focus,
      drawBackground = true,
      backgroundColor = Color.Brown)
  }
}
