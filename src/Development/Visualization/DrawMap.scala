package Development.Visualization

import Geometry.TileRectangle
import Startup.With
import Utilities.Caching.CacheFrame
import bwapi.{Color, Player, Position}
import Utilities.Enrichment.EnrichPosition._

object DrawMap {
  
  def text(
    origin:Position,
    text:String) {
    With.game.drawTextMap(origin, text)
  }
  
  def line(
    start:Position,
    end:Position,
    color:Color = Color.Grey) = {
    With.game.drawLineMap(start, end, color)
  }
  
  def box(
    start:Position,
    end:Position,
    color:Color = Color.Grey) = {
    With.game.drawBoxMap(start, end, color)
  }
  
  def circle(
    center:Position,
    radius:Int,
    color:Color = Color.Grey) {
    With.game.drawCircleMap(center, radius, color)
  }
  
  def label(
    text:String,
    position:Position,
    drawBackground:Boolean = false,
    backgroundColor:Color = Color.Grey) {
    labelBox(List(text), position, drawBackground, backgroundColor)
  }
  
  def labelBox(
      textLines:Iterable[String],
      position:Position,
      drawBackground:Boolean = false,
      backgroundColor:Color = Color.Grey) {
    val horizontalMargin = 2
    val estimatedTextWidth = (9 * textLines.map(_.size).max) / 2
    val boxWidth = estimatedTextWidth + 2 * horizontalMargin
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
    With.game.drawBoxMap(rectangle.startPosition, rectangle.endPosition, color)
  }
  
  def polygonPositions(points:Iterable[Position], color:bwapi.Color = bwapi.Color.Brown) {
    points.reduce((p1, p2) => { With.game.drawLineMap(p1, p2, color); p2 })
    With.game.drawLineMap(points.head, points.last, color)
  }
  
  def playerColor(player:Player):Color = {
    if (player.isNeutral) Color.Grey
    else if (player.isEnemy(With.self)) Color.Red
    else Color.Blue
  }
  
  def viewport:TileRectangle = viewportCache.get
  private val viewportCache = new CacheFrame[TileRectangle](() => viewportRecalculate)
  private def viewportRecalculate:TileRectangle =
    new TileRectangle(
      With.game.getScreenPosition.toTilePosition,
      With.game.getScreenPosition.toTilePosition.add(640/32, 480/32))
}
