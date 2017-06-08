package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Views.Geography.VisualizeGeography
import Debugging.Visualizations.Views.View

object ViewHappy extends View {
  
  def render() {
    VisualizeBlackScreen.render()
    VisualizeGeography.render()
    VisualizeVectorUnits.render()
    VisualizeBullets.render()
    VisualizeBlackScreenOverlay.render("Retro Arcade Happy Vision")
  }
}
