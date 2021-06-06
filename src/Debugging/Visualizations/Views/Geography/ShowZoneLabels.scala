package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Information.Geography.Types.Zone
import Lifecycle.With

object ShowZoneLabels extends View {
  
  override def renderMap() {
    With.geography.zones.foreach(renderZone)
  }
  
  def renderZone(zone: Zone) {
    zone.exit.foreach(exit => DrawMap.arrow(zone.centroid.center, zone.centroid.center.project(exit.pixelCenter, 128.0), zone.owner.colorDark))
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
