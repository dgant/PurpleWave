package Placement.Generation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.{Tile, TileRectangle}
import Placement.Templating.Template
import bwapi.Color

case class Fit(origin: Tile, template: Template) {
  def drawMap(): Unit = {
    if (template.points.isEmpty) return
    val startX = template.points.map(_.point.x).min
    val startY = template.points.map(_.point.y).min
    val endX = template.points.map(p => p.point.x + p.requirement.width).max
    val endY = template.points.map(p => p.point.y + p.requirement.height).max
    DrawMap.tileRectangle(TileRectangle(origin.add(startX, startY), origin.add(endX, endY)), Color.White)
    template.points.foreach(_.drawMap(origin))
  }
}
