package Mathematics.Shapes

import Mathematics.Points.{Point, Tile}

object Pylons {
  // Pylon power is weird.
  // The formula for which top-left tiles  are legal for building placement is a bit obtuse due to the actual formula being based around the centers of units.
  //
  // JohnJ/jaj22 confirmed the accuracy of Skynet's pylon range reference:
  // https://github.com/Laccolith/skynet/blob/399018f41b49fbb55a0ea32142117e97e9d2f9ae/Skynet/PylonPowerTracker.cpp#L54
  //
  // JohnJ's grid:
  // http://pastebin.com/0PiTvGpK

  val points2: Array[Point] =
    (-8 to 7).flatten(x =>
      (-4 to 4).map(y =>
        Point(x, y)))
      .filter(point =>
        point.y match {
          case -4|3       =>  point.x >= -6 && point.x <= 5
          case -3|2       =>  point.x >= -7 && point.x <= 6
          case -2| -1|0|1 =>  point.x >= -7
          case 4          =>  point.x >= -3 && point.x <= 2
          case _          =>  throw new IndexOutOfBoundsException
        })
      .toArray

   val points3: Array[Point] =
    (-8 to 7).flatten(x =>
      (-5 to 4).map(y =>
        Point(x, y)))
      .filter(point =>
        point.y match {
          case -5|4       =>  point.x >= -4 && point.x <= 1
          case -4|3       =>  point.x >= -7 && point.x <= 4
          case -3|2       =>  point.x <= 5
          case -2| -1|0|1 =>  point.x <= 6
          case _          =>  throw new IndexOutOfBoundsException
        })
      .toArray

  val map2: Set[(Int, Int)] = points2.map(p => (p.x, p.y)).toSet
  val map3: Set[(Int, Int)] = points3.map(p => (p.x, p.y)).toSet

  def powers2(from: Tile, to: Tile): Boolean = map2.contains((to.x - from.x, to.y - from.y))
  def powers3(from: Tile, to: Tile): Boolean = map3.contains((to.x - from.x, to.y - from.y))
}
