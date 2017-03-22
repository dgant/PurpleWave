package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Startup.With

import scala.collection.JavaConverters._

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
