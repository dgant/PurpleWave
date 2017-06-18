package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Views.Geography.MapGeography
import Debugging.Visualizations.Views.View

object ViewTextOnly extends View {
  
  def render() {
    ScreenBlackScreen.render()
    MapTextOnly.render()
    MapGeography.render()
    ScreenBlackScreenOverlay.render("Work-Friendly Retro Console Vision")
  }
}
