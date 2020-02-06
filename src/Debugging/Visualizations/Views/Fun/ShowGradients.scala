package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Mathematics.Points.Pixel
import bwapi.Color

object ShowGradients extends View {

  def drawGradient(row: Int, column: Int, color: (Int, Int) => Color): Unit = {
    val origin = Pixel(264 * row, 264 * column)
    var i = 0
    val scale = 16
    while (i < 256 / scale) {
      var j = 0
      while (j < 256 / scale) {
        val start = origin.add(scale * i, scale * j)
        DrawMap.box(start, start.add(scale, scale), color(scale * i, scale * j), solid = true)
        j += 1
      }
      i += 1
    }
  }

  override def renderMap(): Unit = {
    drawGradient(0, 0, (x, y) => new Color(x, y, 0))
    drawGradient(0, 1, (x, y) => new Color(x, 0, y))
    drawGradient(0, 2, (x, y) => new Color(0, x, y))
    drawGradient(1, 0, (x, y) => new Color(x, y, 255))
    drawGradient(1, 1, (x, y) => new Color(x, 255, y))
    drawGradient(1, 2, (x, y) => new Color(255, x, y))
    drawGradient(2, 0, (x, y) => Colors.hsv(x, y, 255))
    drawGradient(2, 1, (x, y) => Colors.hsv(x, 255, y))
    drawGradient(2, 2, (x, y) => Colors.hsv(128, x, y))
  }
}
