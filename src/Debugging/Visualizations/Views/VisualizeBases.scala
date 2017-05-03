package Debugging.Visualizations.Views

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With

object VisualizeBases {
  
  def render() {
    With.geography.zones.foreach(zone => {
  
      val battle = With.battles.byZone(zone)
      val estimation = battle.estimation
      zone.bases.foreach(base => {
        
        
        DrawMap.tileRectangle(base.harvestingArea,    Colors.DarkGreen)
        DrawMap.tileRectangle(base.townHallArea,      base.zone.owner.colorDark)
        DrawMap.labelBox(
          Vector(
            base.zone.owner.name,
            if (base.isStartLocation) "Start location" else "",
            "Minerals left: " + base.mineralsLeft,
            "Gas left:     " + base.gasLeft,
            if (base.lastScoutedFrame <= With.frame+ 24 ) ""
            else if (base.lastScoutedFrame <= 0) "Never scouted"
            else "Last scouted " + (With.frame - base.lastScoutedFrame) + " frames ago",
            "+" +
              VisualizeBattles.formatGain(estimation.costToEnemy)
              + " vs -"
              + VisualizeBattles.formatGain(estimation.costToUs)
          ),
          base.townHallArea.midPixel,
          true,
          base.zone.owner.colorDark)
      })
    })
  }
}
