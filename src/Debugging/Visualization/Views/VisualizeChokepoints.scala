package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Startup.With
import bwapi.Color

object VisualizeChokepoints {
  
  def render() {
    With.geography.zones.foreach(zone => {
  
      zone.edges.foreach(edge => {
        DrawMap.labelBox(
          List(edge.zones.map(_.centroid.toString).mkString(" -> ")),
          edge.centerPixel)
        DrawMap.circle(edge.centerPixel, edge.radiusPixels.toInt, Color.Purple)
      })
    })
  }
}
