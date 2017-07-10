package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Views.Geography.ShowZones
import Debugging.Visualizations.Views.View

object ShowHappyVision extends View {
  
  def render() {
    ShowBlackScreen.renderScreen()
    ShowZones.renderMap()
    ShowHappyUnits.renderMap()
    ShowBulletsAsHearts.renderMap()
    ShowPlayerNames.renderScreen("Retro Arcade Happy Vision")
  }
}
