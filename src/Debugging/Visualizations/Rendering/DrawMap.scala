package Debugging.Visualizations.Rendering

import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.Positions.TileRectangle
import Utilities.EnrichPosition._
import bwapi.{Color, Player, Position}

object DrawMap {
  
  def text(
    origin:Position,
    text:String) {
    if (irrelevant(origin)) return
    With.game.drawTextMap(origin, text)
  }
  
  def line(
    start:Position,
    end:Position,
    color:Color = Colors.DefaultGray) {
    if (irrelevant(Vector(start, end))) return
    With.game.drawLineMap(start, end, color)
  }
  
  def box(
           start:Position,
           end:Position,
           color:Color = Colors.DefaultGray,
           solid:Boolean = false) {
    if (irrelevant(Vector(start, end))) return
    With.game.drawBoxMap(start, end, color, solid)
  }
  
  def circle(
              center:Position,
              radius:Int,
              color:Color = Colors.DefaultGray,
              solid:Boolean = false) {
    if (irrelevant(
      Vector(
        center,
        center.add(radius, radius),
        center.add(-radius, radius),
        center.add(radius, -radius),
        center.add(-radius, -radius)))) return
    With.game.drawCircleMap(center, radius, color, solid)
  }
  
  def triangle(
                position1:Position,
                position2:Position,
                position3:Position,
                color:Color = Colors.DefaultGray,
                solid: Boolean = false) {
    if (irrelevant(Vector(position1, position2, position3))) return
    With.game.drawTriangleMap(position1, position2, position3, color, solid)
  }
  
  def label(
    text:String,
    position:Position,
    drawBackground:Boolean = false,
    backgroundColor:Color = Colors.DefaultGray) {
    if (irrelevant(position)) return
    labelBox(Vector(text), position, drawBackground, backgroundColor)
  }
  
  def labelBox(
      textLines:Iterable[String],
      position:Position,
      drawBackground:Boolean = false,
      backgroundColor:Color = Colors.DefaultGray) {
    
    if (irrelevant(position)) return
    
    val horizontalMargin = 2
    val estimatedTextWidth = (9 * textLines.map(_.size).max) / 2
    val boxWidth = estimatedTextWidth + (if (estimatedTextWidth > 0) 2 * horizontalMargin else 0)
    val boxHeight = 11 * textLines.size
    val textX = position.getX - boxWidth/2
    val textY = position.getY - boxHeight/2
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
    With.game.drawTextMap(
      textX,
      textY,
      textLines.mkString("\n"))
  }
  
  def tileRectangle(rectangle:TileRectangle, color:Color) {
    if (irrelevant(Vector(rectangle.startPixel, rectangle.endPixel))) return
    With.game.drawBoxMap(rectangle.startPixel, rectangle.endPixel, color)
  }
  
  def polygonPositions(points:Iterable[Position], color:Color = Colors.DefaultGray) {
    points.reduce((p1, p2) => { line(p1, p2, color); p2 })
    line(points.head, points.last, color)
  }
  
  def irrelevant(points:Iterable[Position]):Boolean = {
    points.forall(irrelevant)
  }
  def irrelevant(pixel:Position):Boolean = {
    val buffer = 32 * 4
    ! pixel.valid || ! With.viewport.contains(pixel)
  }
}
