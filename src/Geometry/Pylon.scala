package Geometry

import bwapi.TilePosition

object Pylon {
  
  //Handy reference:
  //https://github.com/tscmoo/tsc-bwai/blob/6c93fd0877da55b101c8b48a8ac5d7bce60561f6/src/pylons.h
  
  def powers(pylon:TilePosition, target:TilePosition):Boolean = {
    val x = _normalize(pylon.getX - target.getX)
    val y = _normalize(pylon.getY - target.getY)
    
    return y match {
      case 0 => x < 7 && x > 1
      case 1 => x < 7 && x > 1
      case 2 => x < 6
      case 4 => x < 4
      case _ => false
    }
  }
  
  def _normalize(value:Int):Int = {
    if (value < 0) -value else value - 1
  }
}
