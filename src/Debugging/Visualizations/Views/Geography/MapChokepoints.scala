package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With

object MapChokepoints {
  
  def render() {
    
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
