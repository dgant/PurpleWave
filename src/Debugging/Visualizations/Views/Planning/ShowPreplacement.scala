package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.{Direction, Pixel, SpecificPoints, TileGenerator}

object ShowPreplacement extends View {

  override def renderMap(): Unit = {
    With.placement.fits.foreach(_.drawMap())
  }

  def showGenerator() {
    With.geography.zones.foreach(zone => {
      if (zone.boundary.contains(new Pixel(With.game.getMousePosition).add(With.viewport.start).tile)) {
        val bounds        = zone.boundary
        val exitDirection = zone.exit.map(_.direction).getOrElse(zone.centroid.subtract(SpecificPoints.tileMiddle).direction)
        val exitTile      = zone.exit.map(_.pixelCenter.tile).getOrElse(zone.centroid)
        val tilesFront    = bounds.cornerTilesInclusive.sortBy(t => Math.min(Math.abs(t.x - exitTile.x), Math.abs(t.y - exitTile.y))).take(2)
        val tilesBack     = bounds.cornerTilesInclusive.filterNot(tilesFront.contains)
        val cornerFront   = tilesFront.maxBy(_.tileDistanceSquared(SpecificPoints.tileMiddle))
        val cornerBack    = tilesBack.maxBy(_.tileDistanceSquared(cornerFront))

        val directionToBack   = new Direction(cornerFront, cornerBack)
        val directionToFront  = new Direction(cornerBack, cornerFront)

        DrawMap.box(bounds.startPixel, bounds.endPixel, Colors.NeonYellow)
        val generator = new TileGenerator(exitTile, zone.boundary.startInclusive, zone.boundary.endExclusive, directionToBack)
        var previous = exitTile
        var next = previous
        var i = 0
        while (generator.hasNext) {
          next = generator.next()
          DrawMap.arrow(previous.center, next.center, Colors.NeonOrange)
          DrawMap.label(i.toString, next.center.add(0, 7))
          i += 1
          previous = next
        }
      }
    })
  }
}
