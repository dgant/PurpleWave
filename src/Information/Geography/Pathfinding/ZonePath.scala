package Information.Geography.Pathfinding

import Information.Geography.Types.Zone

case class ZonePath(
  from  : Zone,
  to    : Zone,
  steps : Vector[ZonePathNode]) {
  
  // Goes through centroids of all zones which is silly, but avoids reliance on BWTA ground distance
  lazy val lengthPixels: Double = steps.map(_.length).sum
}
