package Debugging.Visualization

import Startup.With

object DrawScreen {
  
  def header(x:Int, text:String) = {
    With.game.drawTextScreen(x, 5, text)
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
