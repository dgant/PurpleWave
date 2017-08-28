package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Micro.Squads.Squad
import ProxyBwapi.UnitInfo.UnitInfo

object ShowSquads extends View {
  
  override def renderMap() {
    With.squads.squadsByPriority.reverse.filter(_.recruits.nonEmpty).foreach(renderSquadMap)
  }
  
  def renderSquadMap(squad: Squad) {
    val color = squadColors(squad.hashCode % squadColors.length)
    (squad.recruits ++ squad.enemies).foreach(unit =>
      DrawMap.label(
        squad.goal.toString,
        unit.pixelCenter.add(0, unit.unitClass.height),
        drawBackground = true,
        color,
        drawBorder = unit.isEnemy,
        unit.player.colorMedium))
  
    With.game.drawCircleMap(squad.centroid.bwapi, squad.recruits.map(_.pixelDistanceSlow(squad.centroid)).max.toInt, color)
  }
  
  override def renderScreen() {
    
    val table =
      Vector(Vector("Client", "Goal", "", "", "", "Recruits", "", "", "Enemies")) ++
      With.squads.squadsByPriority.map(squad =>
        Vector(
          squad.client.toString,
          squad.goal.toString,
          "",
          "",
          "",
          enumerateUnits(squad.recruits),
          "",
          "",
          enumerateUnits(squad.enemies)))
  
    DrawScreen.table(5, 7 * With.visualization.lineHeightSmall, table)
  }
  
  def enumerateUnits(units: Iterable[UnitInfo]): String = {
    
    val counts = units
      .map(_.unitClass)
      .groupBy(x => x)
    
    val output = counts
      .toSeq
      .sortBy(-_._2.size)
      .map(p => p._2.size + " " + p._1)
      .mkString(", ")
    
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
