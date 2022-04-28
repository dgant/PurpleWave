package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Points.{Pixel, TileRectangle}
import Tactic.Production.BuildBuilding

object ShowFoundations extends DebugView {

  override def renderMap(): Unit = {
    var i = 1
    var lastCorners: Iterable[Pixel] = Iterable.empty
    With.tactics.produce.queue
      .view
      .filter(_.isInstanceOf[BuildBuilding])
      .map(_.asInstanceOf[BuildBuilding])
      .foreach(production => production.foundation.foreach(foundation => {
        val box = TileRectangle(
          foundation.tile,
          foundation.tile.add(
            production.buildingClass.tileWidthPlusAddon,
            production.buildingClass.tileHeight))
        DrawMap.box(box.startPixel.add(8, 8), box.endPixel.subtract(8, 8), Colors.MediumGreen)
        DrawMap.label(f"#$i ${production.buildingClass}", box.bottomCenterPixel.subtract(0, With.visualization.lineHeightSmall / 2))
        if (lastCorners.nonEmpty) {
          val arrow = lastCorners.flatMap(b => box.cornerPixels.map(a => (a, b))).minBy(p => p._1.pixelDistance(p._2))
          DrawMap.arrow(arrow._1, arrow._2, Colors.DarkGreen)
        }
        lastCorners = box.cornerPixels
        i += 1
      }))
  }
}
