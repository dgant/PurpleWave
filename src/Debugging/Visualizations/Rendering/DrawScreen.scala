package Debugging.Visualizations.Rendering

import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.Points.Pixel
import bwapi.Color

object DrawScreen {
  
  def header(x: Int, text: String) {
    column(x, With.visualization.lineHeightSmall, text)
  }
  
  def column(x: Int, y: Int, text: Iterable[String]) {
    column(x, y, text.mkString("\n"))
  }
  
  def column(x: Int, y:Int, text: String) {
    With.game.drawTextScreen(x, y, text)
  }
  
  def table(x: Int, y: Int, cells: Iterable[Iterable[String]]) {
    cells.zipWithIndex.foreach(pair => tableRow(x, y, pair._2, pair._1))
  }
  
  def tableRow(x: Int, y: Int, rowIndex: Int, row: Iterable[String]) {
    row.zipWithIndex.foreach(pair =>
      With.game.drawTextScreen(
        x + pair._2 * 60,
        y + rowIndex * 13,
        pair._1))
  }
  
  class GraphCurve(
    val color:  Color,
    val points: IndexedSeq[Double])
  
  def graph(
    start:    Pixel,
    label:    String,
    curves:   Traversable[GraphCurve],
    fixedMin: Option[Double] = None,
    fixedMax: Option[Double] = None,
    color0:   Color = Colors.DeepViolet,
    color1:   Color = Colors.MidnightViolet,
    width:    Int = 90,
    height:   Int = 90 + With.visualization.lineHeightSmall,
    margin:   Int = 2) {
    
    val end               = start.add(width, height)
    val innerBorderStart  = start             .add      (margin, margin + With.visualization.lineHeightSmall)
    val innerBorderEnd    = end               .subtract (margin, margin)
    val pointStart        = innerBorderStart  .add      (margin, margin)
    val pointEnd          = innerBorderEnd    .subtract (margin, margin)
    
    val dataMin = curves.flatten(_.points).min
    val dataMax = curves.flatten(_.points).max
    val min     = Math.min(dataMin, fixedMin.getOrElse(dataMin))
    val max     = Math.max(dataMax, fixedMax.getOrElse(dataMax))
    val points  = curves.map(_.points.size).max
    val scaleX  = (pointEnd.x - pointStart.x) / points.toDouble
    val scaleY  = (pointEnd.y - pointStart.y) / Math.max(1.0, max - min)
    
    With.game.drawBoxScreen(start.bwapi, end.bwapi, color0, true)
    With.game.drawBoxScreen(innerBorderStart.bwapi, innerBorderEnd.bwapi, color1, true)
    With.game.drawTextScreen(start.add(margin, 0).bwapi, label)
    
    curves.foreach(curve => {
      var i = 0
      while (i < curve.points.size - 1) {
        With.game.drawLineScreen(
          (pointStart.x + scaleX * i                        ).toInt,
          (pointStart.y + scaleY * (max - curve.points(i  ))).toInt,
          (pointStart.x + scaleX * (i + 1)                  ).toInt,
          (pointStart.y + scaleY * (max - curve.points(i+1))).toInt,
          curve.color)
        i += 1
      }
    })
  }
}
