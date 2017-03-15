package Development.Overlay

import Global.Combat.Battle.Battle
import Startup.With
import bwapi.Color
import Utilities.Enrichment.EnrichPosition._

object DrawBattles {
  def draw = With.battles.all.foreach(_drawBattle)
  
  def _drawBattle(battle:Battle) {
    //if (battle.enemy.strength * battle.us.strength == 0) return
    //if (battle.us.vanguard.getDistance(battle.enemy.vanguard) > 32 * 20) return
    With.game.drawCircleMap(battle.focus, 8, Color.Brown)
    With.game.drawCircleMap(battle.us.vanguard, 8, Color.Blue)
    With.game.drawCircleMap(battle.enemy.vanguard, 8, Color.Red)
    With.game.drawLineMap(battle.focus, battle.us.vanguard, Color.Blue)
    With.game.drawLineMap(battle.focus, battle.enemy.vanguard, Color.Red)
    With.game.drawBoxMap(
      battle.us.units.map(_.pixel).minBound,
      battle.us.units.map(_.pixel).maxBound,
      Color.Blue)
    With.game.drawBoxMap(
      battle.enemy.units.map(_.pixel).minBound,
      battle.enemy.units.map(_.pixel).maxBound,
      Color.Red)
    Draw.label(
      List(battle.us.strength/100 + " - " + battle.enemy.strength/100),
      battle.focus,
      drawBackground = true,
      backgroundColor = Color.Brown)
  }
}
