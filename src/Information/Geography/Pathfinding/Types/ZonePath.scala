package Information.Geography.Pathfinding.Types

import Information.Geography.Types.Zone

case class ZonePath(
  from  : Zone,
  to    : Zone,
  steps : Vector[ZonePathNode]) {
  
  def zones: Vector[Zone] = Vector(from) ++ steps.map(_.to)
  
  // Goes through centroids of all zones which is silly, but avoids reliance on BWTA ground distance
  lazy val lengthPixels: Double = steps.map(_.length).sum
}
