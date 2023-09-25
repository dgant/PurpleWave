package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import bwapi.Text

object ShowPlayerNames extends {
  
  def renderScreen(visionLabel: String): Unit = {
    With.game.setTextSize(Text.Size.Large)
    With.game.drawBoxScreen   (5,      5,      5 + 20, 5 + 20,        With.self.colorMedium,  true)
    With.game.drawBoxScreen   (5,      5 + 25, 5 + 20, 5 + 20 + 25,   With.enemy.colorMedium, true)
    DrawScreen.text  (5 + 25, 5,                             With.self.name + renderRace(With.self) + " vs. ")
    DrawScreen.text  (5 + 25, 5 + 25,                        With.enemy.name + renderRace(With.enemy))
    With.game.setTextSize(Text.Size.Default)
    DrawScreen.text  (5,      5 + 50,                        With.self.name + " is displaying this game in " + visionLabel + "!")
    With.game.setTextSize(Text.Size.Small)
  }
  
  private def renderRace(player: PlayerInfo): String = {
    " (" + player.raceCurrent.toString.take(1) + ")"
  }
}
