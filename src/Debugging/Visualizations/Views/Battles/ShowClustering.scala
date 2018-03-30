package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.Micro.ShowSquads
import Debugging.Visualizations.Views.View
import Information.Battles.Types.BattleLocal
import Lifecycle.With
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
    for (i <- battles.indices) {
      renderMapBattle(battles(i), colors(i % colors.size))
    }
  }
  
  private def renderMapBattle(battle: BattleLocal, color: Color) {
    battle.teams.foreach(team =>
      team.units.foreach(unit =>
        DrawMap.line(unit.pixelCenter, team.centroid, color)))
  }
  
  private lazy val colors = Vector(
    Colors.NeonRed,
    Colors.BrightYellow,
    Colors.NeonOrange,
    Colors.BrightGreen,
    Colors.BrightTeal,
    Colors.NeonBlue,
    Colors.NeonIndigo,
    Colors.NeonViolet
  )
}

