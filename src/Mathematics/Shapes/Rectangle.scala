package Mathematics.Shapes

import Mathematics.Points.Point

object Rectangle {
  
  def pointsFromCenter(xRadius:Int, yRadius:Int):Iterable[Point] =
    (-yRadius to yRadius).flatten(dy =>
      (-xRadius to xRadius).map(dx =>
        Point(dx, dy)))
  
  def pointsFromTopLeft(width:Int, height:Int):Iterable[Point] =
    (0 until height).flatten(dy =>
      (0 until width).map(dx =>
        Point(dx, dy)))
}
