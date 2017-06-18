package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Views.Geography.MapGeography
import Debugging.Visualizations.Views.View

object ViewHappy extends View {
  
  def render() {
    ScreenBlackScreen.render()
    MapGeography.render()
    MapVectorUnits.render()
    MapBullets.render()
    ScreenBlackScreenOverlay.render("Retro Arcade Happy Vision")
  }
}
