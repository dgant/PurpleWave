package Debugging.Visualization.Views

import Debugging.Visualization.Colors
import Debugging.Visualization.Rendering.DrawMap
import Startup.With

object VisualizeBases {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      zone.bases.foreach(base => {
        DrawMap.tileRectangle(base.harvestingArea,    Colors.DarkGreen)
        DrawMap.tileRectangle(base.townHallRectangle, DrawMap.playerColorDark(base.zone.owner))
        DrawMap.labelBox(
          List(
            base.zone.owner.getName,
            if (base.isStartLocation) "Start location" else "",
            "Minerals left: " + base.mineralsLeft,
            "Gas left:      " + base.gasLeft,
            if (base.lastScoutedFrame <= With.frame+ 24 ) ""
            else if (base.lastScoutedFrame <= 0) "Never scouted"
            else "Last scouted " + (With.frame - base.lastScoutedFrame) + " frames ago"
          ),
          base.townHallRectangle.midPixel,
          true,
          DrawMap.playerColorDark(base.zone.owner))
      })
    })
  }
}
