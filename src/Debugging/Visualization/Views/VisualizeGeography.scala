package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Startup.With

import scala.collection.JavaConverters._

object VisualizeGeography {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      DrawMap.polygonPositions(
        zone.bwtaRegion.getPolygon.getPoints.asScala,
        DrawMap.playerColorDark(zone.owner))
      
      DrawMap.line(
        zone.bwtaRegion.getPolygon.getPoints.asScala.head,
        zone.bwtaRegion.getPolygon.getPoints.asScala.last,
        DrawMap.playerColorDark(zone.owner))
    })
  }
}
