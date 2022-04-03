package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Maff
import Tactic.Squads.Squad
import ProxyBwapi.UnitInfo.UnitInfo

object ShowSquads extends View {
  
  override def renderMap() {
    With.squads.all.filter(_.units.nonEmpty).foreach(renderSquadMap)
  }
  
  def renderSquadMap(squad: Squad) {
    val color = squadColors(squad.hashCode % squadColors.length)
    squad.units.foreach(unit =>
      DrawMap.label(
        squad.toString,
        unit.pixel.add(0, unit.unitClass.height),
        drawBackground = true,
        backgroundColor = color,
        drawBorder = unit.isEnemy,
        borderColor = With.enemy.colorMedium))

    val centroid = Maff.centroid(squad.units.view.map(_.pixel))
    val hull = Maff.convexHull(squad.units.flatMap(_.corners))
    DrawMap.polygon(hull, color)

    squad.targets.foreach(q => {
      var i = 0
      if (q.nonEmpty) {
        val targetColor = Colors.BrightYellow
        DrawMap.crosshair(q.head.pixel, q.head.unitClass.dimensionMax / 2, targetColor)
        while (i < q.length - 1) {
          DrawMap.arrow(q(i).pixel, q(i + 1).pixel, Colors.BrightRed)
          i += 1
        }
      }
    })
  }
  
  override def renderScreen() {
    val table =
      Vector(Vector("Goal", "", "Vicinity", "", "", "Recruits", "",  "Targets", "", "Enemies")) ++
      With.squads.all.map(squad =>
        Vector(
          squad.toString,
          "",
          squad.vicinity.base.map(_.toString).getOrElse(squad.vicinity.tile.toString),
          "",
          "",
          enumerateUnits(squad.units),
          "",
          enumerateUnits(squad.targets.getOrElse(Seq.empty)),
          "",
          enumerateUnits(squad.enemies)))
    DrawScreen.table(5, 7 * With.visualization.lineHeightSmall, table)
  }
  
  def enumerateUnits(units: Iterable[UnitInfo]): String = {
    
    val counts = units
      .toVector
      .map(_.unitClass)
      .groupBy(x => x)
    
    val output = counts
      .toSeq
      .sortBy(-_._2.size)
      .map(p => p._2.size + p._1.abbr)
      .mkString(".")
    
    output
  }
  
  
  lazy val squadColors = Vector(
    Colors.MidnightRed,
    Colors.MidnightOrange,
    Colors.MidnightYellow,
    Colors.MidnightGreen,
    Colors.MidnightBlue,
    Colors.MidnightIndigo,
    Colors.MidnightViolet,
    Colors.DarkRed,
    Colors.DarkOrange,
    Colors.DarkYellow,
    Colors.DarkGreen,
    Colors.DarkBlue,
    Colors.DarkIndigo,
    Colors.DarkViolet,
    Colors.MediumRed,
    Colors.MediumOrange,
    Colors.MediumYellow,
    Colors.MediumGreen,
    Colors.MediumBlue,
    Colors.MediumIndigo,
    Colors.MediumViolet,
    Colors.BrightRed,
    Colors.BrightOrange,
    Colors.BrightYellow,
    Colors.BrightGreen,
    Colors.BrightBlue,
    Colors.BrightIndigo,
    Colors.BrightViolet
  )
}
