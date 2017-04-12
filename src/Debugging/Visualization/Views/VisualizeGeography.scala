package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Lifecycle.With

object VisualizeGeography {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      DrawMap.polygonPositions(
        zone.points,
        zone.owner.colorDark)
      
      DrawMap.line(
        zone.points.head,
        zone.points.last,
        zone.owner.colorDark)
    })
  }
}
