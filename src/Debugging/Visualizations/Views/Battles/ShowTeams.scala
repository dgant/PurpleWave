package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import bwapi.Color

object ShowTeams extends View {

  override def renderMap(): Unit = {
    ShowBattles.localBattle.foreach(battle => {
      val team = battle.us
      val bright = Colors.BrightTeal
      val medium = Colors.MediumTeal
      val dark = Colors.DarkTeal
      DrawMap.circle(team.centroidAir, 16, bright)
      DrawMap.box(team.centroidGround.subtract(16, 16), team.centroidGround.add(16, 16), bright)
      DrawMap.arrow(team.centroidGround, team.centroidGround.project(team.lineWidth(),   team.widthIdeal() / 2), bright)
      DrawMap.arrow(team.centroidGround, team.centroidGround.project(team.lineWidth(), - team.widthIdeal() / 2), bright)
      team.units.foreach(unit => {
        DrawMap.line(unit.pixelCenter, unit.teamHorizontalSlot(), medium)
        DrawMap.line(unit.pixelCenter, unit.teamWidthGoal(), dark)
        DrawMap.box(
          unit.teamHorizontalSlot().subtract(unit.unitClass.dimensionLeft, unit.unitClass.dimensionUp),
          unit.teamHorizontalSlot().add(unit.unitClass.dimensionRight, unit.unitClass.dimensionDown),
          medium)
        DrawMap.circle(unit.teamWidthGoal(), unit.unitClass.radialHypotenuse.toInt, dark)
      })
      DrawMap.label(f"W: ${(team.coherenceWidth() * 100).toInt}%%", team.centroidGround.add(0, 16), drawBackground = true, backgroundColor = Color.Black)
      DrawMap.label(f"D: ${(team.coherenceDepth() * 100).toInt}%%", team.centroidGround.add(0, 26), drawBackground = true, backgroundColor = Color.Black)
    })
  }
}
