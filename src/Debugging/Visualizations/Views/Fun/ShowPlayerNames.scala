package Debugging.Visualizations.Views.Fun

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo

object ShowPlayerNames extends {
  
  def renderScreen(visionLabel: String) {
    With.game.setTextSize(bwapi.TextSize.Large)
    With.game.drawBoxScreen   (5,      5,      5 + 20, 5 + 20,        With.self.colorMedium,  true)
    With.game.drawBoxScreen   (5,      5 + 25, 5 + 20, 5 + 20 + 25,   With.enemy.colorMedium, true)
    With.game.drawTextScreen  (5 + 25, 5,                             With.self.name + renderRace(With.self) + " vs. ")
    With.game.drawTextScreen  (5 + 25, 5 + 25,                        With.enemy.name + renderRace(With.enemy))
    With.game.setTextSize(bwapi.TextSize.Default)
    With.game.drawTextScreen  (5,      5 + 50,                        With.self.name + " is displaying this game in " + visionLabel + "!")
    With.game.setTextSize(bwapi.TextSize.Small)
  }
  
  private def renderRace(player: PlayerInfo): String = {
    " (" + player.raceCurrent.toString.take(1) + ")"
  }
}
