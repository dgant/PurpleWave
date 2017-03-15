package Development.Visualization

import Startup.With
import bwapi.Color
import scala.collection.JavaConverters._
import Utilities.Enrichment.EnrichPosition._

object VisualizeGeography {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      DrawMap.polygonPositions(zone.region.getPolygon.getPoints.asScala)
      
      DrawMap.line(
        zone.region.getPolygon.getPoints.asScala.head,
        zone.region.getPolygon.getPoints.asScala.last,
        bwapi.Color.Brown)
      
      DrawMap.label(
        List(zone.region.getCenter.toString, zone.region.getCenter.toTilePosition.toString, zone.owner.getName),
        zone.region.getCenter)
      
      zone.edges.foreach(edge => {
        DrawMap.label(
          List(edge.zones.map(_.centroid.toString).mkString(" -> ")),
          edge.chokepoint.getCenter)
        DrawMap.circle(edge.chokepoint.getCenter, edge.chokepoint.getWidth.toInt/2, Color.Purple)
        DrawMap.line(edge.chokepoint.getSides.first, edge.chokepoint.getSides.second, Color.Purple)
      })
      
      zone.bases.foreach(base => {
        DrawMap.tileRectangle(base.harvestingArea, Color.Cyan)
        DrawMap.tileRectangle(base.townHallArea, Color.Yellow)
        DrawMap.label(
          List(base.zone.owner.getName, if (base.isStartLocation) "Start location" else ""),
          base.townHallArea.startInclusive.topLeftPixel,
          true,
          DrawMap.playerColor(base.zone.owner))
      })
    })
  }
}
