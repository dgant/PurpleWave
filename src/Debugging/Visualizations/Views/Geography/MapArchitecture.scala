package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With

object MapArchitecture {
  
  def render() {
    With.architect.exclusions.foreach(exclusion => {
      DrawMap.tileRectangle(exclusion.area, Colors.MediumRed)
      DrawMap.label(exclusion.description, exclusion.area.midPixel)
    })
  }
}
