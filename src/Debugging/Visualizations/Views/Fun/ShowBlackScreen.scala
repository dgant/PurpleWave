package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Views.View
import Lifecycle.With
import bwapi.Color

object ShowBlackScreen extends View {
  
  override def renderScreen() {
    With.game.drawBoxScreen(0, 0, 1500, 1200, Color.Black, true)
  }
}
