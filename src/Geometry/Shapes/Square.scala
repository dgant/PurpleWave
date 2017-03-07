package Geometry.Shapes

import Geometry.Point

object Square {
  
  def points(width:Int):Iterable[Point] = Rectangle.points(width, width)
  
  def pointsDownAndRight(count:Int):Iterable[Point] =
    (0 until count).flatten(dy =>
      (0 until count).map(dx =>
        new Point(dx, dy)))
}
