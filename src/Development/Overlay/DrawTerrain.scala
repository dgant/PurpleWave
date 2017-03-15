package Development.Overlay

import Startup.With
import bwapi.Color
import scala.collection.JavaConverters._
import Utilities.Enrichment.EnrichPosition._

object DrawTerrain {
  def draw() {
    With.geography.zones.foreach(zone => {
      Draw.polygonPositions(zone.region.getPolygon.getPoints.asScala)
      With.game.drawLineMap(
        zone.region.getPolygon.getPoints.asScala.head,
        zone.region.getPolygon.getPoints.asScala.last,
        bwapi.Color.Brown)
      Draw.label(
        List(zone.region.getCenter.toString, zone.region.getCenter.toTilePosition.toString, zone.owner.getName),
        zone.region.getCenter)
      
      zone.edges.foreach(edge => {
        Draw.label(
          List(edge.zones.map(_.centroid.toString).mkString(" -> ")),
          edge.chokepoint.getCenter)
        With.game.drawCircleMap(edge.chokepoint.getCenter, edge.chokepoint.getWidth.toInt/2, Color.Purple)
        With.game.drawLineMap(edge.chokepoint.getSides.first, edge.chokepoint.getSides.second, Color.Purple)
      })
      
      zone.bases.foreach(base => {
        Draw.tileRectangle(base.harvestingArea, Color.Cyan)
        Draw.tileRectangle(base.townHallArea, Color.Yellow)
        Draw.label(
          List(base.zone.owner.getName, if (base.isStartLocation) "Start location" else ""),
          base.townHallArea.startInclusive.topLeftPixel,
          true,
          Draw.playerColor(base.zone.owner))
      })
    })
  }
}
