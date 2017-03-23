package Debugging.Visualization.Views

import Startup.With
import bwapi.Color

object VisualizeBlackScreen {
  
  def render() {
    With.game.drawBoxScreen(0, 0, 1500, 1200, Color.Black, true)
    //With.grids.friendlyVision.tiles
  }
}
