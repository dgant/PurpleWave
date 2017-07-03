package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowArchitecture extends View {
  
  override def renderMap() {
  
    With.architecture.exclusions.foreach(exclusion => {
      DrawMap.tileRectangle(exclusion.areaExcluded, Colors.MediumRed)
      DrawMap.label(exclusion.description, exclusion.areaExcluded.midPixel)
    })
  
    With.architecture.unwalkable.foreach(tile => DrawMap.box(
      tile.topLeftPixel.add(8, 8),
      tile.topLeftPixel.add(24, 24),
      Colors.MediumOrange,
      solid = false))
  
    With.architecture.unbuildable.foreach(tile => DrawMap.box(
      tile.topLeftPixel.add(4, 4),
      tile.topLeftPixel.add(28, 28),
      Colors.MediumTeal,
      solid = false))
  }
}
