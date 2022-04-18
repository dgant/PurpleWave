package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowZoneBorders extends DebugView {
  
  override def renderMap() {
    With.geography.zones.foreach(zone => {
      zone.perimeter.foreach(tile =>
        DrawMap.box(
          tile.topLeftPixel,
          tile.bottomRightPixel,
          Colors.DarkGray,
          solid = false))

      zone.border.foreach(tile => {
        val color = Colors.White
        if ( ! tile.up.valid || tile.up.zone != zone) {
          DrawMap.line(tile.topLeftPixel, tile.topRightPixel, color)
        }
        if ( ! tile.down.valid || tile.down.zone != zone) {
          DrawMap.line(tile.bottomLeftPixel, tile.bottomRightPixel, color)
        }
        if ( ! tile.left.valid || tile.left.zone != zone) {
          DrawMap.line(tile.topLeftPixel, tile.bottomLeftPixel, color)
        }
        if ( ! tile.right.valid || tile.right.zone != zone) {
          DrawMap.line(tile.topRightPixel, tile.bottomRightPixel, color)
        }
      })
    })

    With.geography.zones.foreach(_.edges.foreach(e => DrawMap.circle(e.pixelCenter, e.radiusPixels.toInt, Colors.MediumTeal)))
  }
}
