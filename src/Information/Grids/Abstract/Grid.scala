package Information.Grids.Abstract

import Startup.With
import bwapi.TilePosition

abstract class Grid[T] {
  
  val width = With.mapWidth
  val height = With.mapHeight
  
  protected val length:Int          = width * height
  def update()                      = {}
  def valid(i:Int):Boolean          = i > 0 && i < length
  def i(tileX:Int, tileY:Int)       = tileX + tileY * width
  def x(i:Int):Int                  = i % width
  def y(i:Int):Int                  = i / width
  def get(i:Int):T
  def get(tileX:Int, tileY:Int):T   = get(i(tileX, tileY))
  def get(tile: TilePosition):T     = get(i(tile.getX, tile.getY))
}
