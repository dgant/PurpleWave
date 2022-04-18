package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView

object ShowTeams extends DebugView {

  override def renderMap(): Unit = {
    ShowBattles.localBattle.foreach(battle => {
      val team = battle.us
      val bright = Colors.BrightTeal
      val medium = Colors.MediumTeal
      val dark = Colors.DarkTeal
      DrawMap.circle(team.centroidAir, 16, bright)
      DrawMap.box(team.centroidGround.subtract(16, 16), team.centroidGround.add(16, 16), bright)
    })
  }
}
