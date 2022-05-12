package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.TileRectangle

object ShowZones extends DebugView {
  override def renderMap(): Unit = {
    With.geography.zones.foreach(renderZoneMap)
  }
  def renderZoneMap(zone: Zone): Unit = {
    // Draw boundaries
    zone.perimeter.foreach(tile => DrawMap.cross(tile.center, 4, Colors.MediumGray))
    zone.border.foreach(tile => {
      val color = zone.owner.colorMedium
      if ( ! tile.up.valid || tile.up.zone != zone) {
        DrawMap.line(tile.topLeftPixel.add(8, 1), tile.topRightPixel.add(-8, 1), color)
      }
      if ( ! tile.down.valid || tile.down.zone != zone) {
        DrawMap.line(tile.bottomLeftPixel.add(8, -1), tile.bottomRightPixel.add(-8, -1), color)
      }
      if ( ! tile.left.valid || tile.left.zone != zone) {
        DrawMap.line(tile.topLeftPixel.add(1, 8), tile.bottomLeftPixel.add(1, -8), color)
      }
      if ( ! tile.right.valid || tile.right.zone != zone) {
        DrawMap.line(tile.topRightPixel.add(-1, 8), tile.bottomRightPixel.add(-1, -8), color)
      }
    })

    // Draw edges
    zone.edges.foreach(edge =>
      DrawMap.line(
        edge.sidePixels.head,
        edge.sidePixels.last,
        edge.zones.find( ! _.owner.isNeutral).map(_.owner).getOrElse(With.neutral).colorDark))

    // Draw bases
    zone.bases.foreach(base => {
      base.resourcePathTiles.foreach(tile => DrawMap.cross(tile.center, 2, Colors.MediumRed))
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
    zone.exitOriginal.foreach(exit => DrawMap.arrow(zone.centroid.center, zone.centroid.center.project(exit.pixelCenter, 128.0), zone.owner.colorDark))
    DrawMap.label(zone.toString, zone.centroid.center, drawBackground = true, backgroundColor = zone.owner.colorDark)
    zone.edges.foreach(edge =>
      edge.zones.foreach(edgeZone => {
        val labelPixel = edge.pixelCenter.project(edgeZone.centroid.center, 32)
        val color = edgeZone.owner.colorDark
        DrawMap.line(edge.pixelCenter, labelPixel, color)
        DrawMap.label(edgeZone.name, labelPixel, drawBackground = true, backgroundColor = color)
      }))
  }
}
