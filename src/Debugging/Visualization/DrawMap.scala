package Debugging.Visualization

import Geometry.TileRectangle
import Startup.With
import Performance.Caching.CacheFrame
import bwapi.{Color, Player, Position}
import Utilities.TypeEnrichment.EnrichPosition._

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
    color:Color = Color.Grey) {
    if (irrelevant(List(start, end))) return
    With.game.drawLineMap(start, end, color)
  }
  
  def box(
    start:Position,
    end:Position,
    color:Color = Color.Grey) {
    if (irrelevant(List(start, end))) return
    With.game.drawBoxMap(start, end, color)
  }
  
  def circle(
    center:Position,
    radius:Int,
    color:Color = Color.Grey) {
    if (irrelevant(
      List(
        center,
        center.add(radius, radius),
        center.add(-radius, radius),
        center.add(radius, -radius),
        center.add(-radius, -radius)))) return
    With.game.drawCircleMap(center, radius, color)
  }
  
  def label(
    text:String,
    position:Position,
    drawBackground:Boolean = false,
    backgroundColor:Color = Color.Grey) {
    if (irrelevant(position)) return
    labelBox(List(text), position, drawBackground, backgroundColor)
  }
  
  def labelBox(
      textLines:Iterable[String],
      position:Position,
      drawBackground:Boolean = false,
      backgroundColor:Color = Color.Grey) {
    
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
    if (irrelevant(List(rectangle.startPixel, rectangle.endPixel))) return
    With.game.drawBoxMap(rectangle.startPixel, rectangle.endPixel, color)
  }
  
  def polygonPositions(points:Iterable[Position], color:bwapi.Color = bwapi.Color.Brown) {
    if (irrelevant(points)) return
    points.reduce((p1, p2) => { With.game.drawLineMap(p1, p2, color); p2 })
    With.game.drawLineMap(points.head, points.last, color)
  }
  
  def playerColor(player:Player):Color = {
    if      (player == With.self)     Color.Purple
    else if (player == With.neutral)  Color.Cyan
    else                              Color.Red
  }
  
  def irrelevant(points:Iterable[Position]):Boolean = {
    points.forall(irrelevant)
  }
  def irrelevant(point:Position):Boolean = {
    point.getX < viewportStart.getX - 64 ||
    point.getX > viewportEnd.getX + 64 ||
    point.getY < viewportStart.getY - 64 ||
    point.getY > viewportEnd.getY + 64
  }
  
  def viewportStart:Position = viewportStartCache.get
  def viewportEnd:Position = viewportEndCache.get
  private val viewportStartCache = new CacheFrame[Position](() => viewportStartRecalculate)
  private val viewportEndCache = new CacheFrame[Position](() => viewportEndRecalculate)
  private def viewportStartRecalculate:Position = With.game.getScreenPosition
  private def viewportEndRecalculate:Position   = With.game.getScreenPosition.add(2*640, 2*480)
}
