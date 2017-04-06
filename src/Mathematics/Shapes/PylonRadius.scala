package Mathematics.Shapes

import bwapi.TilePosition

object PylonRadius {
  
  // Pylon power is kind of tricky. You can use BWAPI's hasPower, but that's slow and only works for Pylons that are already complete.
  // References:
  //
  // Skynet:
  // https://github.com/Laccolith/skynet/blob/399018f41b49fbb55a0ea32142117e97e9d2f9ae/Skynet/PylonPowerTracker.cpp
  //
  // JohnJ's grid:
  // http://pastebin.com/0PiTvGpK
  //
  // Moo's grid:
  // https://github.com/tscmoo/tsc-bwai/blob/6c93fd0877da55b101c8b48a8ac5d7bce60561f6/src/pylons.h
  
  val points = Rectangle
    .pointsFromTopLeft(16, 10)
    .filter(point =>
      isInRadius(
        normalize(point.x - 8),
        normalize(point.y - 5))
    )
    .toList
  
  def powers(pylon:TilePosition, target:TilePosition):Boolean = {
    isInRadius(
      normalize(pylon.getX - target.getX),
      normalize(pylon.getY - target.getY))
  }
  
  def isInRadius(x:Int, y:Int):Boolean = y match {
    case 0 => x < 7 && x > 1
    case 1 => x < 7 && x > 1
    case 2 => x < 6
    case 4 => x < 4
    case _ => false
  }
  
  private def normalize(value:Int):Int = {
    if (value < 0) -value else value - 1
  }
}
