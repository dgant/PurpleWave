package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Points.TileRectangle

object ShowZones extends DebugView {
  
  override def renderMap(): Unit = {
    // Draw boundaries
    With.geography.zones.foreach(zone => {
      zone.perimeter.foreach(tile => DrawMap.cross(tile.center, 4, Colors.DarkGray))
      zone.border.foreach(tile => {
        val color = zone.owner.colorBright
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

    // Draw edges
    With.geography.zones.foreach(_.edges.foreach(edge => DrawMap.line(edge.sidePixels.head, edge.sidePixels.last, edge.zones.find( ! _.owner.isNeutral).map(_.owner).getOrElse(With.neutral).colorDark)))

    // Draw bases
    With.geography.bases.foreach(base => {
      base.resourcePathTiles.foreach(tile => DrawMap.circle(tile.center, 16, Colors.MediumRed))
      DrawMap.tileRectangle(base.harvestingArea,  Colors.DarkGreen)
      DrawMap.tileRectangle(base.townHallArea,    base.owner.colorDark)
      DrawMap.labelBox(
        Vector(
          base.description,
          "Resources: " + base.mineralsLeft + "m + " + base.gasLeft + "g",
          if (With.framesSince(base.lastFrameScoutedByUs) < 24 * 5) ""
          else if (base.lastFrameScoutedByUs <= 0) "Unscouted"
          else "Last scouted " + With.framesSince(base.lastFrameScoutedByUs) + " frames ago"
        ),
        base.townHallArea.center,
        drawBackground = true,
        base.owner.colorDark)
      DrawMap.tileRectangle(new TileRectangle(base.heart), Colors.BrightYellow)
    })

    // Draw labels
    With.geography.zones.foreach(zone => {
      zone.exitOriginal.foreach(exit => DrawMap.arrow(zone.centroid.center, zone.centroid.center.project(exit.pixelCenter, 128.0), zone.owner.colorDark))
      DrawMap.label(zone.toString, zone.centroid.center, drawBackground = true, backgroundColor = zone.owner.colorDark)
      zone.edges.foreach(edge =>
        edge.zones.foreach(edgeZone => {
          val labelPixel = edge.pixelCenter.project(edgeZone.centroid.center, 32)
          val color = edgeZone.owner.colorDark
          DrawMap.line(edge.pixelCenter, labelPixel, color)
          DrawMap.label(edgeZone.name, labelPixel, drawBackground = true, backgroundColor = color)
        }))
    })
  }
}
