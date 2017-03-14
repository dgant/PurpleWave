package Geometry.Grids.Abstract

import Startup.With
import bwapi.TilePosition

abstract class Grid[T] {
  
  val _width = With.game.mapWidth
  val _height = With.game.mapHeight
  
  def update()                                 = {}
  def valid(i:Int):Boolean                     = i > 0 && i < _width * _height
  def i(tileX:Int, tileY:Int)                  = tileX + tileY * _width
  def x(i:Int):Int                             = i % _width
  def y(i:Int):Int                             = i / _width
  def get(i:Int):T
  def get(tileX:Int, tileY:Int):T              = get(i(tileX, tileY))
  def get(tile: TilePosition):T                = get(i(tile.getX, tile.getY))
}
