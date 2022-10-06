package Information.Geography.Pathfinding.Types

import Information.Geography.Types.Zone

case class ZonePath(
  from  : Zone,
  to    : Zone,
  steps : Vector[ZonePathNode]) {

  lazy val zones: Vector[Zone] = Vector(from) ++ steps.map(_.to)
  lazy val length: Double = {
    var i = 0
    var distance = 0
    var here = from.centroid
    while (i < steps.length) {
      val edge = steps(i).edge
      distance += edge.distanceGrid.get(here)
      here = edge.tiles.minBy(_.tileDistanceSquared(here))
      i += 1
    }
    distance += here.groundPixels(to.centroid)
    distance
  }
}
