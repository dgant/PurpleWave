package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowZoneBorders extends View {
  
  override def renderMap() {
    With.geography.zones.foreach(zone => {
      zone.border.foreach(tile =>
        DrawMap.box(
          tile.topLeftPixel,
          tile.bottomRightPixel,
          zone.owner.colorMedium,
          solid = false))
  
      zone.perimeter.foreach(tile =>
        DrawMap.circle(
          tile.pixelCenter,
          14,
          zone.owner.colorMedium,
          solid = false))
    })

    With.geography.zones.foreach(_.edges.foreach(e => DrawMap.circle(e.pixelCenter, e.radiusPixels.toInt, Colors.MediumTeal)))
  }
}
