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
      DrawMap.label(zone.name, zone.centroid.pixelCenter, drawBackground = true, backgroundColor = zone.owner.colorDark)
    })
  
    With.geography.zones.foreach(zone => {
      zone.edges.foreach(edge => {
        val owner = edge.zones.find( ! _.owner.isNeutral).map(_.owner).getOrElse(With.neutral)
        DrawMap.labelBox(
          Vector(
            edge.zones.map(_.centroid.toString).mkString(" -> ")
          ),
          edge.centerPixel)
      
        DrawMap.line(
          edge.sidePixels.head,
          edge.sidePixels.last,
          owner.colorDark
        )
      
        DrawMap.circle(
          edge.centerPixel,
          edge.radiusPixels.toInt,
          owner.colorDark)
      })
    })
  }
}
