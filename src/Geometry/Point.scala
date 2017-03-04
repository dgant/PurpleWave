package Geometry

import bwapi.{Position, TilePosition}

class Point(val x:Int, val y:Int) {
  def position:Position = new Position(x, y)
  def tile:TilePosition = new TilePosition(x, y)
}
