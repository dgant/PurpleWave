package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Startup.With

object VisualizeGeography {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      DrawMap.polygonPositions(
        zone.points,
        DrawMap.playerColorDark(zone.owner))
      
      DrawMap.line(
        zone.points.head,
        zone.points.last,
        DrawMap.playerColorDark(zone.owner))
    })
  }
}
