package Information.Grids.Construction

import Mathematics.Points.Point

class GridPsi2x2and3x2 extends AbstractGridPsi {
  
  // Pylon power is weird.
  // Either the Starcraft formula for pylon power is obtuse or the legal positions are hardcoded.
  //
  // JohnJ/jaj22 confirmed the accuracy of Skynet's pylon range reference:
  // https://github.com/Laccolith/skynet/blob/399018f41b49fbb55a0ea32142117e97e9d2f9ae/Skynet/PylonPowerTracker.cpp#L54
  
  override val psiPoints:Array[Point] =
    (-8 to 7).flatten(x =>
      (-4 to 4).map(y =>
        new Point(x, y)))
      .filter(point =>
        point.y match {
          case -4|3         =>  point.x >= -6 && point.x <= 5
          case -3|2         =>  point.x >= -7 && point.x <= 6
          case -2| -1|0|1   =>  point.x >= -7
          case 4            =>  point.x >= -3 && point.x <= 2
          case _            =>  throw new IndexOutOfBoundsException
        })
      .toArray
}
