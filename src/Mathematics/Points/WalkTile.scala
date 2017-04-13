package Mathematics.Points

import Mathematics.Pixels.Point
import bwapi.WalkPosition

case class WalkTile(argX:Int, argY:Int) extends AbstractPoint(argX, argY) {
  
  def bwapi:WalkPosition = new WalkPosition(x, y)
  
  def add(dx:Int, dy:Int):WalkTile = {
    new WalkTile(x + dx, y + dy)
  }
  def add(point:Point):WalkTile = {
    add(point.x, point.y)
  }
}
