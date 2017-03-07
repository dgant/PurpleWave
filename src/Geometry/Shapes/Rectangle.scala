package Geometry.Shapes

import Geometry.Point

object Rectangle {
  
  def points(width:Int, height:Int):Iterable[Point] =
    (-height to height).flatten(dy =>
      (-width to width).map(dx =>
        new Point(dx, dy)))
}
