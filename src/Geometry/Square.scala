package Geometry

object Square {
  
  def points(width:Int):Iterable[Point] =
    (-width to width).flatten(dy =>
      (-width to width).map(dx =>
        new Point(dx, dy)))
}
