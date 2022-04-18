package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.Micro.ShowSquads
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowDivisions extends DebugView {
  
  override def renderScreen() {
    val y = 17 * With.visualization.lineHeightSmall
    val x0 = 5
    val x1 = 220
    val rows0 = With.battles.divisions.map(_.enemies)
    val rows1 = With.battles.divisions.map(_.bases)
    DrawScreen.column(x0, y, "Division units: " +: rows0.map(renderScreenUnits))
    DrawScreen.column(x1, y, "Division bases: " +: rows1.map(_.map(_.toString).mkString))
  }
  
  private def renderScreenUnits(units: Iterable[UnitInfo]): String = f"${units.size}: ${ShowSquads.enumerateUnits(units)}"
  
  override def renderMap() {
    With.battles.divisions.foreach(division => {
      val enemyPoints = division.enemies.flatMap(u =>
        u.corners
          .map(p => p.add(
            3 * Maff.toSign(p.x > u.x),
            3 * Maff.toSign(p.y > u.y))))
          .toVector
      val hull = Maff.convexHull(enemyPoints)
      DrawMap.polygon(hull.view.map(_.asPixel), Color.White)
    })
  }
}

