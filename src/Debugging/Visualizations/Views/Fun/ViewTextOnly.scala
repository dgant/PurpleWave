package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Views.Geography.VisualizeGeography
import Debugging.Visualizations.Views.View

object ViewTextOnly extends View {
  
  def render() {
    VisualizeBlackScreen.render()
    VisualizeTextOnly.render()
    VisualizeGeography.render()
    VisualizeBlackScreenOverlay.render("Work-Friendly Retro Console Vision")
  }
}
