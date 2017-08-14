package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowZoneLabels extends View {
  
  override def renderMap() {
    
    With.geography.zones.foreach(zone => {
      DrawMap.label(zone.name, zone.centroid.pixelCenter, drawBackground = true, backgroundColor = zone.owner.colorDark)
    })
  
    With.geography.zones.foreach(zone => {
      zone.edges.foreach(edge => {
        edge.zones.foreach(edgeZone => {
          val labelPixel = edge.centerPixel.project(edgeZone.centroid.pixelCenter, 32)
          val color = edgeZone.owner.colorDark
          DrawMap.line(edge.centerPixel, labelPixel, color)
          DrawMap.label(edgeZone.name, labelPixel, drawBackground = true, backgroundColor = color)
        })
      })
    })
  }
}
