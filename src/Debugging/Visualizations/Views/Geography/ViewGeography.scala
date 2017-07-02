package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ViewGeography extends View {
  
  def render() {
    ScreenGroundskeeper.render()
    VisualizeArchitecture.render()
  
    With.geography.zones.foreach(zone =>
      zone.border.foreach(tile =>
        DrawMap.box(
          tile.topLeftPixel,
          tile.bottomRightPixel,
          Colors.MediumGray,
          solid = false)))
  }
}
