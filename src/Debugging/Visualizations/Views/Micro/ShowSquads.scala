package Debugging.Visualizations.Views.Micro

import Debugging.EnumerateUnits
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.DebugView
import Debugging.Visualizations.{Colors, Hues}
import Lifecycle.With
import Mathematics.Maff
import Tactic.Squads.Squad

object ShowSquads extends DebugView {
  
  override def renderMap(): Unit = {
    With.squads.all.filter(_.units.nonEmpty).foreach(renderSquadMap)
  }
  
  def renderSquadMap(squad: Squad): Unit = {
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
        while (i < q.size - 1) {
          DrawMap.arrow(q(i).pixel, q(i + 1).pixel, Colors.hsv(Hues.Red, 255, 255 - 255 * i / q.size))
          i += 1
        }
      }
    })
  }
  
  override def renderScreen(): Unit = {
    val table =
      Vector(Vector("Goal", "", "Vicinity", "", "", "Recruits", "",  "Targets", "", "Enemies")) ++
      With.squads.all.map(squad =>
        Vector(
          squad.toString,
          "",
          squad.vicinity.base.map(_.toString).getOrElse(squad.vicinity.tile.toString),
          "",
          "",
          EnumerateUnits(squad.units),
          "",
          EnumerateUnits(squad.targets.getOrElse(Seq.empty)),
          "",
          EnumerateUnits(squad.enemies)))
    DrawScreen.table(5, 7 * With.visualization.lineHeightSmall, table)
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
