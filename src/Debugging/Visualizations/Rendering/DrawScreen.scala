package Debugging.Visualizations.Rendering

import Lifecycle.With

object DrawScreen {
  
  def header(x:Int, text:String) = {
    column(x, 5, text)
  }
  
  def column(x:Int, y:Int, text:String) {
    With.game.drawTextScreen(x, y, text)
  }
  
  def table(x:Int, y:Int, cells:Iterable[Iterable[String]]) {
    cells.zipWithIndex.foreach(pair => tableRow(x, y, pair._2, pair._1))
  }
  
  def tableRow(x:Int, y:Int, rowIndex:Int, row:Iterable[String]) {
    row.zipWithIndex.foreach(pair =>
      With.game.drawTextScreen(
        x + pair._2 * 60,
        y + rowIndex * 13,
        pair._1))
  }
}
