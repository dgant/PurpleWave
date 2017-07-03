package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Views.Geography.ShowZones
import Debugging.Visualizations.Views.View

object ShowTextOnly extends View {
  
  def render() {
    ShowBlackScreen.renderScreen()
    ShowTextOnlyUnits.renderMap()
    ShowZones.renderMap()
    ShowPlayerNames.render("Work-Friendly Retro Console Vision")
  }
}
