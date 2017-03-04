package Geometry

object Square {
  
  def points(radius:Int):Iterable[Point] =
    (-radius to radius).flatten(dy =>
      (-radius to radius).map(dx =>
        new Point(dx, dy)))
  
  def pointsDownAndRight(count:Int):Iterable[Point] =
    (0 until count).flatten(dy =>
      (0 until count).map(dx =>
        new Point(dx, dy)))
}
