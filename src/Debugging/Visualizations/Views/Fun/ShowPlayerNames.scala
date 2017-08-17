package Debugging.Visualizations.Views.Fun

import Lifecycle.With

object ShowPlayerNames extends {
  
  def renderScreen(visionLabel: String) {
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.game.drawBoxScreen   (5,      5,      5 + 20, 5 + 20,        With.self.colorDark,  true)
    With.game.drawBoxScreen   (5,      5 + 25, 5 + 20, 5 + 20 + 25,   With.enemy.colorDark, true)
    With.game.drawTextScreen  (5 + 25, 5,                             With.self.name + " vs. ")
    With.game.drawTextScreen  (5 + 25, 5 + 25,                        With.enemy.name)
    With.game.setTextSize(bwapi.Text.Size.Enum.Default)
    With.game.drawTextScreen  (5,      5 + 50,                        With.self.name + " is displaying this game in " + visionLabel + "!")
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
}
