package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With

object MapGeography {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      DrawMap.polygonPixels(
        zone.points,
        zone.owner.colorDark)
      
      DrawMap.line(
        zone.points.head,
        zone.points.last,
        zone.owner.colorDark)
    })
  }
}
