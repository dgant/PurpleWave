package Mathematics.Points

import bwapi.WalkPosition

case class WalkTile(argX: Int, argY: Int) extends AbstractPoint(argX, argY) {
  
  def bwapi: WalkPosition = new WalkPosition(x, y)
  
  def add(dx: Int, dy: Int): WalkTile = {
    WalkTile(x + dx, y + dy)
  }
  def add(point: Point): WalkTile = {
    add(point.x, point.y)
  }
}
