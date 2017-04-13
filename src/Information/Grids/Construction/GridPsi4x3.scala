package Information.Grids.Construction

import Mathematics.Pixels.Point

class GridPsi4x3 extends AbstractGridPsi {
  
  // Pylon power is weird.
  // Either the Starcraft formula for pylon power is obtuse or the legal positions are hardcoded.
  //
  // JohnJ/jaj22 confirmed the accuracy of Skynet's pylon range reference:
  // https://github.com/Laccolith/skynet/blob/399018f41b49fbb55a0ea32142117e97e9d2f9ae/Skynet/PylonPowerTracker.cpp#L54
  
  override val psiPoints:Array[Point] =
    (-8 to 7).flatten(x =>
      (-5 to 4).map(y =>
        new Point(x, y)))
      .filter(point =>
        point.y match {
          case -5|4         =>  point.x >= -4 && point.x <= 1
          case -4|3         =>  point.x >= -7 && point.x <= 4
          case -3|2         =>  point.x <= 5
          case -2| -1|0|1   =>  point.x <= 6
          case _            =>  throw new IndexOutOfBoundsException
        })
      .toArray
}
