package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowBases extends View {
  
  override def renderMap() {
    With.geography.zones.foreach(zone => {
  
      val battle = With.battles.byZone(zone)
      val estimation = battle.estimationAbstract
      
      zone.bases.foreach(base => {
        DrawMap.tileRectangle(base.harvestingArea,  Colors.DarkGreen)
        DrawMap.tileRectangle(base.townHallArea,    base.zone.owner.colorDark)
        DrawMap.labelBox(
          Vector(
            base.name + ", " + zone.name,
            base.zone.owner.name,
            Vector(
              if (base.isStartLocation)       Some("Start location")  else None,
              if (base.isNaturalOf.isDefined) Some("Natural")         else None,
              if (base.zone.island)           Some("Island")          else None,
              if (base.zone.contested)        Some("Contested")       else None
            )
            .flatten
            .mkString(", "),
            "Resources: " + base.mineralsLeft + "m + " + base.gasLeft + "g",
            if (With.framesSince(base.lastScoutedFrame) < 24 * 10) ""
            else if (base.lastScoutedFrame <= 0) "Never scouted"
            else "Last scouted " + With.framesSince(base.lastScoutedFrame) + " frames ago",
            "+" +
              estimation.costToEnemy.toInt
              + " vs -"
              + estimation.costToUs.toInt
          ),
          base.townHallArea.midPixel,
          drawBackground = true,
          base.zone.owner.colorDark)
      })
    })
  }
}
