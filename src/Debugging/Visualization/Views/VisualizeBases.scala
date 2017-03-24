package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Startup.With
import Utilities.EnrichPosition._
import bwapi.Color

object VisualizeBases {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      zone.bases.foreach(base => {
        DrawMap.tileRectangle(base.harvestingArea, Color.Cyan)
        DrawMap.tileRectangle(base.townHallRectangle, Color.Yellow)
        DrawMap.labelBox(
          List(base.zone.owner.getName, if (base.isStartLocation) "Start location" else ""),
          base.townHallRectangle.startInclusive.topLeftPixel,
          true,
          DrawMap.playerColor(base.zone.owner))
      })
    })
  }
}
