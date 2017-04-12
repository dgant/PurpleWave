package Debugging.Visualization.Views

import Debugging.Visualization.Colors
import Debugging.Visualization.Rendering.DrawMap
import Lifecycle.With

object VisualizeBases {
  
  def render() {
    With.geography.zones.foreach(zone => {
      
      zone.bases.foreach(base => {
        DrawMap.tileRectangle(base.harvestingArea,    Colors.DarkGreen)
        DrawMap.tileRectangle(base.townHallArea,      base.zone.owner.colorDark)
        DrawMap.labelBox(
          List(
            base.zone.owner.name,
            if (base.isStartLocation) "Start location" else "",
            "Minerals left: " + base.mineralsLeft,
            "Gas left:     " + base.gasLeft,
            if (base.lastScoutedFrame <= With.frame+ 24 ) ""
            else if (base.lastScoutedFrame <= 0) "Never scouted"
            else "Last scouted " + (With.frame - base.lastScoutedFrame) + " frames ago",
            VisualizeBattles.formatStrength(With.battles.byZone(zone).us.strength)
              + " vs "
              + VisualizeBattles.formatStrength(With.battles.byZone(zone).enemy.strength)
          ),
          base.townHallArea.midPixel,
          true,
          base.zone.owner.colorDark)
      })
    })
  }
}
