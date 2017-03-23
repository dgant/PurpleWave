package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Startup.With
import bwapi.Color

object VisualizeBlackScreen {
  
  def render(visionLabel:String) {
    With.game.drawBoxScreen(0, 0, 1500, 1200, Color.Black, true)
    //With.grids.friendlyVision.tiles
  
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.game.drawBoxScreen   (5,      5,      5 + 20, 5 + 20,        DrawMap.playerColor(With.self),       true)
    With.game.drawBoxScreen   (5,      5 + 25, 5 + 20, 5 + 20 + 25,   DrawMap.playerColor(With.game.enemy), true)
    With.game.drawTextScreen  (5 + 25, 5,                             With.game.self.getName + " vs. ")
    With.game.drawTextScreen  (5 + 25, 5 + 25,                        With.game.enemy.getName)
    With.game.setTextSize(bwapi.Text.Size.Enum.Default)
    With.game.drawTextScreen  (5,      5 + 50,                        "PurpleWave is displaying this game in " + visionLabel + "!")
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
}
