package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.Micro.ShowSquads
import Debugging.Visualizations.Views.View
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowClustering extends View {
  
  override def renderScreen() {
    val y = 7 * With.visualization.lineHeightSmall
    val x0 = 5
    val x1 = 325
    val rows0 = With.battles.local.map(_.us.units)
    val rows1 = With.battles.local.map(_.enemy.units)
    DrawScreen.column(x0, y, rows0.map(renderScreenUnits))
    DrawScreen.column(x1, y, rows1.map(renderScreenUnits))
  }
  
  private def renderScreenUnits(units: Seq[UnitInfo]): String = {
    "(" + units.size + ") " + ShowSquads.enumerateUnits(units)
  }
  
  override def renderMap() {
    val battles = With.battles.local
    battles.foreach(b => Seq((b.us, With.self.colorBright), (b.enemy, With.enemy.colorBright)).foreach(p =>
        DrawMap.polygonPixels(
          PurpleMath.convexHullPixels(
            p._1.units.map(_.corners.maxBy(_.pixelDistanceSquared(b.focus)))),
            p._2)
    ))
  }
  
  private def renderMapBattle(battle: BattleLocal, color: Color) {
    battle.teams.foreach(team =>
      team.units.foreach(unit =>
        DrawMap.line(unit.pixelCenter, team.centroid, color)))
  }
}

