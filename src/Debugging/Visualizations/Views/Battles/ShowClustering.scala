package Debugging.Visualizations.Views.Battles

import Debugging.EnumerateUnits
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.DebugView
import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowClustering extends DebugView {
  
  override def renderScreen(): Unit = {
    val y = 7 * With.visualization.lineHeightSmall
    val x0 = 5
    val x1 = 325
    val rows0 = With.battles.local.map(_.us.units)
    val rows1 = With.battles.local.map(_.enemy.units)
    DrawScreen.column(x0, y, "Battle, friendly: " +: rows0.map(describeUnits))
    DrawScreen.column(x1, y, "Battle, enemy: "    +: rows1.map(describeUnits))
  }
  
  private def describeUnits(units: Seq[UnitInfo]): String = {
    f"(${units.size}) ${EnumerateUnits(units)}"
  }
  
  override def renderMap(): Unit = {
    val battles = With.battles.local
    battles.foreach(b => Seq(
      (b.us, With.self.colorNeon),
      (b.enemy, With.enemy.colorNeon)).foreach(p =>
        DrawMap.polygon(Maff.convexHull(p._1.units.flatMap(_.cornersInclusive)).map(_.asPixel), p._2)
    ))
  }
  
  private def renderMapBattle(battle: Battle, color: Color): Unit = {
    battle.teams.foreach(team =>
      team.units.foreach(unit =>
        DrawMap.line(unit.pixel, team.centroidAir, color)))
  }
}

