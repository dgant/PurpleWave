package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Views.Geography.ShowZones
import Debugging.Visualizations.Views.DebugView

object ShowTextOnly extends DebugView {
  
  def render() {
    ShowBlackScreen.renderScreen()
    ShowTextOnlyUnits.renderMap()
    ShowZones.renderMap()
    ShowPlayerNames.renderScreen("Work-Friendly Retro Console Vision")
  }
}
