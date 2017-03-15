package Development.Visualization

import Geometry.TileRectangle
import Startup.With
import bwapi.{Color, Player, Position}

object DrawMap {
  def label(
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
  
  
  def table(startX:Int, startY:Int, cells:Iterable[Iterable[String]]) {
    cells.zipWithIndex.foreach(pair => tableRow(startX, startY, pair._2, pair._1))
  }
  
  def tableRow(startX:Int, startY:Int, rowIndex:Int, row:Iterable[String]) {
    row.zipWithIndex.foreach(pair => With.game.drawTextScreen(
      startX + pair._2 * 50,
      startY + rowIndex * 13,
      pair._1))
  }
}
