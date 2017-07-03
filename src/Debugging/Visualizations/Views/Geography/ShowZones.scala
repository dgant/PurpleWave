package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowZones extends View {
  
  override def renderMap() {
    
    With.geography.zones.foreach(zone => {
      DrawMap.polygonPixels(
        zone.points,
        zone.owner.colorDark)
    })
  
    With.geography.zones.foreach(zone => {
      zone.edges.foreach(edge => {
        DrawMap.labelBox(
          Vector(
            edge.zones.map(_.centroid.toString).mkString(" -> ")
          ),
          edge.centerPixel)
      
        DrawMap.line(
          edge.sidePixels.head,
          edge.sidePixels.last,
          zone.owner.colorDark
        )
      
        DrawMap.circle(
          edge.centerPixel,
          edge.radiusPixels.toInt,
          zone.owner.colorDark)
      })
    })
  }
}
