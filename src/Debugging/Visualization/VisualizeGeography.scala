package Debugging.Visualization

import Startup.With
import bwapi.Color
import scala.collection.JavaConverters._
import Utilities.TypeEnrichment.EnrichPosition._

object VisualizeGeography {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      DrawMap.polygonPositions(zone.region.getPolygon.getPoints.asScala)
      
      DrawMap.line(
        zone.region.getPolygon.getPoints.asScala.head,
        zone.region.getPolygon.getPoints.asScala.last,
        bwapi.Color.Brown)
    })
  }
}
