package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.TileRectangle

object ShowBases extends View {
  
  override def renderMap() {
    With.geography.bases.foreach(base => {
      base.resourcePathTiles.foreach(tile => DrawMap.circle(tile.center, 16, Colors.MediumRed))
      DrawMap.tileRectangle(base.harvestingArea,  Colors.DarkGreen)
      DrawMap.tileRectangle(base.townHallArea,    base.owner.colorDark)
      DrawMap.labelBox(
        Vector(
          base.toString,
          Vector(
            if (base.isStartLocation)       Some("Start location")  else None,
            if (base.isNaturalOf.isDefined) Some("Natural")         else None,
            if (base.zone.island)           Some("Island")          else None
          )
          .flatten
          .mkString(", "),
          "Resources: " + base.mineralsLeft + "m + " + base.gasLeft + "g",
          if (With.framesSince(base.lastScoutedFrame) < 24 * 10) ""
          else if (base.lastScoutedFrame <= 0) "Never scouted"
          else "Last scouted " + With.framesSince(base.lastScoutedFrame) + " frames ago"
        ),
        base.townHallArea.center,
        drawBackground = true,
        base.owner.colorDark)
        DrawMap.tileRectangle(new TileRectangle(base.heart), Colors.BrightYellow)
    })
  }
}
