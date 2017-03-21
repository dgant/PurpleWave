package Debugging.Visualization

import Startup.With
import bwapi.Color

object VisualizeTextOnly {
  def render() {
    With.game.drawBoxScreen(0, 0, 1500, 1200, Color.Black, true)
    
    With.units.all.filter(_.visible).filter(u => u.complete || u.unitClass.isBuilding).foreach(unit => {
      val color = DrawMap.playerColor(unit.player)
      val text = List(
        unit.player.getName,
        unit.unitClass.toString,
        if (unit.player == With.neutral) "" else unit.totalHealth.toString + "/" + unit.unitClass.maxTotalHealth,
        if (unit.complete) "" else "In progress"
      )
      DrawMap.labelBox(text, unit.pixelCenter, false)
    })
  
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.game.drawTextScreen(10, 5, With.game.self.getName + " vs. " + With.game.enemy.getName)
    With.game.setTextSize(bwapi.Text.Size.Enum.Default)
    With.game.drawTextScreen(10, 30, "This game is being displayed in PurpleWave Console Vision")
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
}
