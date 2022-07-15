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
        val drawBox = box.toPixels.expand(-8, -8)
        DrawMap.box(drawBox.startPixel, drawBox.endPixel, Colors.DarkGreen)
        DrawMap.label(f"#$i ${production.buildingClass}", box.bottomCenterPixel.subtract(0, With.visualization.lineHeightSmall / 2))
        if (lastCorners.nonEmpty) {
          val arrow = lastCorners.flatMap(b => drawBox.cornerPixels.map(a => (a, b))).minBy(p => Math.abs(96 - p._1.pixelDistance(p._2)))
          DrawMap.arrow(arrow._2, arrow._1, Colors.MediumGreen)
        }
        lastCorners = drawBox.cornerPixels
        i += 1
      }))
  }
}
