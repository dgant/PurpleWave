package Debugging.Visualization

import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.Color

object VisualizeBases {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      zone.bases.foreach(base => {
        DrawMap.tileRectangle(base.harvestingArea, Color.Cyan)
        DrawMap.tileRectangle(base.townHallArea, Color.Yellow)
        DrawMap.labelBox(
          List(base.zone.owner.getName, if (base.isStartLocation) "Start location" else ""),
          base.townHallArea.startInclusive.topLeftPixel,
          true,
          DrawMap.playerColor(base.zone.owner))
      })
    })
  }
}
