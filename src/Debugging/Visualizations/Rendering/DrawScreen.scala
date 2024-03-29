package Debugging.Visualizations.Rendering

import Debugging.Asciify
import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import bwapi.Color

object DrawScreen {

  def text(x: Int, y: Int, s: String): Unit = {
    With.game.drawTextScreen(x, y, Asciify(s))
  }

  def text(pixel: Pixel, s: String): Unit = {
    text(pixel.x, pixel.y, s)
  }

  def column(x: Int, y: Int, s: Iterable[String]): Unit = {
    column(x, y, s.mkString("\n"))
  }

  def column(x: Int, y: Int, s: String): Unit = {
    text(x, y, s)
  }
  
  def table(x: Int, y: Int, cells: Iterable[Iterable[String]]): Unit = {
    cells.zipWithIndex.foreach(pair => tableRow(x, y, pair._2, pair._1))
  }
  
  def tableRow(x: Int, y: Int, rowIndex: Int, row: Iterable[String]): Unit = {
    row.zipWithIndex.foreach(pair =>
      text(
        x + pair._2 * 60,
        y + rowIndex * 13,
        pair._1))
  }

  def padTable(rows: Iterable[Iterable[String]]): Iterable[Iterable[String]] = {
    val columns = Maff.max(rows.view.map(_.size)).getOrElse(0)
    val columnWidths = rows.view.map(_.view.map(_.length).toIndexedSeq).reduce((r1, r2) => (0 until columns).map(i => Math.max(r1.view.padTo(columns, 0).drop(i).head, r2.view.padTo(columns, 0).drop(i).head)))
    val max: Int = Maff.max(rows.view.flatten.map(_.length)).getOrElse(0)
    rows.map(_.zipWithIndex.map(p => p._1.padTo(columnWidths(p._2), ' ')))
  }

  def tableToString(cells: Iterable[Iterable[String]]): String = {
    cells.map(_.mkString(" ")).mkString("\n")
  }
  
  case class GraphCurve(color: Color, points: IndexedSeq[Double])
  
  def graph(
    start:    Pixel,
    label:    String,
    curves:   Seq[GraphCurve],
    fixedYMin: Option[Double] = None,
    fixedYMax: Option[Double] = None,
    fixedXMax: Option[Int] = None,
    color0:   Color = Colors.DeepViolet,
    color1:   Color = Colors.MidnightViolet,
    width:    Int = 90,
    height:   Int = 90 + With.visualization.lineHeightSmall,
    margin:   Int = 2): Unit = {

    val end               = start.add(width, height)
    val innerBorderStart  = start             .add      (margin, margin + With.visualization.lineHeightSmall)
    val innerBorderEnd    = end               .subtract (margin, margin)
    val pointStart        = innerBorderStart  .add      (margin, margin)
    val pointEnd          = innerBorderEnd    .subtract (margin, margin)

    if (curves.flatten(_.points).isEmpty) {
      return
    }

    val dataMin = curves.flatten(_.points).min
    val dataMax = curves.flatten(_.points).max
    val yMin    = Math.min(dataMin, fixedYMin.getOrElse(dataMin))
    val yMax    = Math.max(dataMax, fixedYMax.getOrElse(dataMax))
    val xMax    = fixedXMax.getOrElse(curves.map(_.points.size).max)
    val scaleX  = (pointEnd.x - pointStart.x) / xMax.toDouble
    val scaleY  = (pointEnd.y - pointStart.y) / Math.max(1.0, yMax - yMin)
    
    With.game.drawBoxScreen(start.bwapi, end.bwapi, color0, true)
    With.game.drawBoxScreen(innerBorderStart.bwapi, innerBorderEnd.bwapi, color1, true)
    text(start.add(margin, 0), label)
    
    curves.foreach(curve => {
      var i = 0
      while (i < Math.min(xMax, curve.points.size) - 1) {
        With.game.drawLineScreen(
          (pointStart.x + scaleX * i                         ).toInt,
          (pointStart.y + scaleY * (yMax - curve.points(i  ))).toInt,
          (pointStart.x + scaleX * (i + 1)                   ).toInt,
          (pointStart.y + scaleY * (yMax - curve.points(i+1))).toInt,
          curve.color)
        i += 1
      }
    })
  }

  def barChart(columns: Seq[Seq[(Double, Color, String)]], argX: Int = 5, argY: Int = 39, width: Int = 24, argHeight: Int = 255): Unit = {
    var x = argX
    val total = columns.map(_.map(_._1).sum).max
    columns.foreach(column => {
      var y = argY + argHeight
      var labelY = 10000
      column.reverse.filter(_._1 > 0).foreach(item => {
        val height = (item._1 * argHeight / total).toInt
        labelY = Math.min(labelY - With.visualization.lineHeightSmall, y - height / 2 - With.visualization.lineHeightSmall / 2)
        With.game.drawBoxScreen(x, y - height, x + width, y, item._2, true)
        text(x + width + 1, labelY, item._3)
        y -= height
      })
      x += width + column.map(_._3.length * 5 + 1).max.toInt
    })
  }
}
