package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.Micro.ShowSquads
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Point
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowDivisions extends View {
  
  override def renderScreen() {
    val y = 17 * With.visualization.lineHeightSmall
    val x0 = 5
    val x1 = 325
    val rows0 = With.battles.divisions.map(_.enemies)
    val rows1 = With.battles.divisions.map(_.bases)
    DrawScreen.column(x0, y, "Division units: " +: rows0.map(renderScreenUnits))
    DrawScreen.column(x1, y, "Division bases: " +: rows1.map(_.map(_.toString).mkString))
  }
  
  private def renderScreenUnits(units: Iterable[UnitInfo]): String = {
    "(" + units.size + ") " + ShowSquads.enumerateUnits(units)
  }
  
  override def renderMap() {
    With.battles.divisions.foreach(division => {
      val enemyPoints = division.enemies.flatMap(u => u.corners.map(p => Point(
        p.x + 3 * PurpleMath.toSign(p.x > u.x),
        p.y + 3 * PurpleMath.toSign(p.y > u.y)))).toVector
      val hull = PurpleMath.convexHull(enemyPoints)
      DrawMap.polygonPixels(hull.view.map(_.asPixel), Color.White)
    })
  }
}

