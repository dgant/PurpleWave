package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With

object VisualizeRealEstate {
  
  def render() = {
    With.architect.exclusions.foreach(DrawMap.tileRectangle(_, Colors.DarkRed))
  }
}
