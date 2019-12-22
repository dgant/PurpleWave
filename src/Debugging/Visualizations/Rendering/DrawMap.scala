package Debugging.Visualizations.Rendering

import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.Points.{Pixel, TileRectangle}
import bwapi.Color

object DrawMap {
  
  def text(
    origin  : Pixel,
    text    : String) {
    if (irrelevant(origin)) return
    With.game.drawTextMap(origin.bwapi, text)
  }
  
  def line(
    start : Pixel,
    end   : Pixel,
    color : Color = Colors.DefaultGray) {
    if (irrelevant(Vector(start, end))) return
    With.game.drawLineMap(start.bwapi, end.bwapi, color)
  }
  
  def arrow(
    start : Pixel,
    end   : Pixel,
    color : Color = Colors.DefaultGray) {
    if (irrelevant(Vector(start, end))) return
    
    line(start, end, color)
    val angleBack = end.radiansTo(start)
    val angleDiff = Math.PI / 8
    triangle(
      end,
      end.radiateRadians(angleBack + angleDiff, 7),
      end.radiateRadians(angleBack - angleDiff, 7),
      color,
      solid = true)
  }
  
  def box(
    start : Pixel,
    end   : Pixel,
    color : Color = Colors.DefaultGray,
    solid : Boolean = false) {
    if (irrelevant(Vector(start, end))) return
    With.game.drawBoxMap(start.bwapi, end.bwapi, color, solid)
  }
  
  def circle(
    center  : Pixel,
    radius  : Int,
    color   : Color = Colors.DefaultGray,
    solid   : Boolean = false) {
    if (irrelevant(
      Vector(
        center,
        center.add(radius, radius),
        center.add(-radius, radius),
        center.add(radius, -radius),
        center.add(-radius, -radius)))) return
    With.game.drawCircleMap(center.bwapi, radius, color, solid)
  }
  
  def triangle(
    position1 : Pixel,
    position2 : Pixel,
    position3 : Pixel,
    color     : Color = Colors.DefaultGray,
    solid: Boolean = false) {
    if (irrelevant(Vector(position1, position2, position3))) return
    With.game.drawTriangleMap(position1.bwapi, position2.bwapi, position3.bwapi, color, solid)
  }
  
  def label(
    text            : String,
    position        : Pixel,
    drawBackground  : Boolean = false,
    backgroundColor : Color = Colors.DefaultGray,
    drawBorder      : Boolean = false,
    borderColor     : Color = Colors.DefaultGray) {
    if (irrelevant(position)) return
    labelBox(Vector(text), position, drawBackground, backgroundColor, drawBorder, borderColor)
  }
  
  def labelBox(
    textLines       : Iterable[String],
    position        : Pixel,
    drawBackground  : Boolean = false,
    backgroundColor : Color = Colors.DefaultGray,
    drawBorder      : Boolean = false,
    borderColor     : Color = Colors.DefaultGray) {
    
    if (irrelevant(position)) return
    
    val horizontalMargin = 2
    val estimatedTextWidth = (9 * textLines.map(_.length).max) / 2
    val boxWidth = estimatedTextWidth + (if (estimatedTextWidth > 0) 2 * horizontalMargin else 0)
    val boxHeight = 11 * textLines.size
    val textX = position.x - boxWidth/2
    val textY = position.y - boxHeight/2
    val boxX = textX - horizontalMargin
    val boxY = textY
    
    if (drawBackground) {
      With.game.drawBoxMap(
        boxX,
        boxY,
        boxX + boxWidth,
        boxY + boxHeight,
        backgroundColor,
        true) //isSolid
    }
    if (drawBorder) {
      With.game.drawBoxMap(
        boxX - 1,
        boxY - 1,
        boxX + boxWidth + 1,
        boxY + boxHeight + 1,
        borderColor,
        false
      )
    }
    With.game.drawTextMap(
      textX,
      textY,
      textLines.mkString("\n"))
  }
  
  def tileRectangle(rectangle: TileRectangle, color: Color) {
    if (irrelevant(Vector(rectangle.startPixel, rectangle.endPixel))) return
    With.game.drawBoxMap(rectangle.startPixel.bwapi, rectangle.endPixel.bwapi, color)
  }
  
  def polygonPixels(points: Iterable[Pixel], color: Color = Colors.DefaultGray) {
    points.reduce((p1, p2) => { line(p1, p2, color); p2 })
    line(points.head, points.last, color)
  }
  
  def drawSkull(pixel: Pixel, colorDark: Color = Colors.DarkGray, colorBright: Color = Colors.BrightGray, f: Int = 1): Unit = {
    DrawMap.circle(pixel, 5 * f, colorBright,  solid = true)
    DrawMap.circle(pixel, 5 * f, colorDark,    solid = false)
    DrawMap.box(pixel.add(-3 * f, 0 * f), pixel.add(1 + 3 * f, 6 * f), colorBright, solid = true)
    DrawMap.box(pixel.add(-3 * f, 0 * f), pixel.add(1 + 3 * f, 6 * f), colorDark,   solid = false)
    DrawMap.box(pixel.add(-3 * f, 0 * f), pixel.add(1 + 3 * f, 3 * f), colorBright, solid = true)

    if (f == 1) {
      DrawMap.box(pixel.add(-2 * f, -2 * f), pixel.add(0 * f, 0 * f), Color.Black, solid = true)
      DrawMap.box(pixel.add( 1 * f, -2 * f), pixel.add(3 * f, 0 * f), Color.Black, solid = true)
    } else {
      DrawMap.circle(pixel.add(-2 * f, -1 * f), f, Color.Black, solid = true)
      DrawMap.circle(pixel.add( 2 * f, -1 * f), f, Color.Black, solid = true)
    }
    DrawMap.triangle(pixel.add(0, 1 * f), pixel.add(- f, 2 * f), pixel.add(f, 2 * f), Color.Black, solid = true)
    DrawMap.line(pixel.add(-1 * f, 3 * f), pixel.add(-1 * f, 6 * f - 1), colorDark)
    DrawMap.line(pixel.add( 1 * f, 3 * f), pixel.add( 1 * f, 6 * f - 1), colorDark)
  }
  
  def irrelevant(points: Iterable[Pixel]): Boolean = {
    With.configuration.visualizationCullViewport && points.forall(irrelevant)
  }
  def irrelevant(pixel: Pixel): Boolean = {
    val buffer = 32 * 4
    ! pixel.valid || ! With.viewport.contains(pixel)
  }
}
