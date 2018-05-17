package Information.Geography.Pathfinding

import Information.Geography.Types.{Edge, Zone}

case class ZonePathNode(
  from : Zone,
  to   : Zone,
  edge : Edge) {
  
  lazy val zones: Vector[Zone] = Vector(from, to)
  
  lazy val length: Double = edge.zones.map(zone => edge.pixelCenter.pixelDistance(zone.centroid.pixelCenter)).sum
}
